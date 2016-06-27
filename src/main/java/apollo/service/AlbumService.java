package apollo.service;

import static accelerate.util.AccelerateConstants.UNIX_PATH_CHAR;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import accelerate.cache.PropertyCache;
import accelerate.databean.DataMap;
import accelerate.logging.Auditable;
import accelerate.util.AppUtil;
import accelerate.util.FileUtil;
import apollo.model.Mp3Tag;

/**
 * PUT DESCRIPTION HERE
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since 07-Jun-2016
 */
@Component
public class AlbumService {
	/**
	 * 
	 */
	private static Logger LOGGER = LoggerFactory.getLogger(TagService.class);

	/**
	 * 
	 */
	@Autowired
	private PropertyCache apolloProps = null;

	/**
	 * @param aAlbumPath
	 * @return
	 */
	@Auditable
	public DataMap getAlbumTags(String aAlbumPath) {
		final String fileExtn = this.apolloProps.get("apollo.targetExtn");
		LOGGER.debug("Reading tags for all tracks with extn [{}] under [{}]", fileExtn, aAlbumPath);

		Mp3Tag albumTag = new Mp3Tag();
		List<Mp3Tag> tagList = FileUtil.findFilesByExtn(aAlbumPath, fileExtn).parallelStream().map(aFile -> {
			Mp3Tag mp3Tag = new Mp3Tag(aFile);
			albumTag.extractCommonTag(mp3Tag);

			/*
			 * Removing tag values set at album level to reduce data
			 * transmission
			 */
			mp3Tag.setField("language", null);
			mp3Tag.setField("genre", null);
			mp3Tag.setField("mood", null);
			mp3Tag.setField("album", null);
			mp3Tag.setField("year", null);
			mp3Tag.setField("albumArtist", null);
			mp3Tag.setField("artwork", null);

			return mp3Tag;
		}).collect(Collectors.toList());

		albumTag.setField("filePath", aAlbumPath);
		albumTag.clear();

		String albumLibraryPath = StringUtils.join(this.apolloProps.get("apollo.library.root"), UNIX_PATH_CHAR,
				albumTag.getLanguage(), UNIX_PATH_CHAR, albumTag.getGenre(), UNIX_PATH_CHAR, albumTag.getAlbumArtist(),
				UNIX_PATH_CHAR, albumTag.getAlbum());
		boolean addedToLibrary = AppUtil.compare(albumLibraryPath, aAlbumPath);

		DataMap dataMap = new DataMap();
		dataMap.put("albumTag", albumTag);
		dataMap.put("trackTags", tagList);
		dataMap.put("addedToLibrary", addedToLibrary);
		return dataMap;
	}

	/**
	 * @param aAlbumTag
	 * @return
	 */
	@Auditable
	public DataMap addToLibrary(Mp3Tag aAlbumTag) {
		final String fileExtn = this.apolloProps.get("apollo.targetExtn");
		LOGGER.debug("Adding album [{}] at path [{}] to library", aAlbumTag.getAlbum(), aAlbumTag.getFilePath());

		StringBuilder msgBuffer = new StringBuilder();
		String currentAlbumPath = aAlbumTag.getFilePath();
		File currentAlbumFolder = new File(currentAlbumPath);

		/*
		 * rename tracks to <title>.<extn>
		 */
		FileUtil.findFilesByExtn(currentAlbumPath, fileExtn).parallelStream().forEach(aFile -> {
			Mp3Tag mp3Tag = new Mp3Tag(aFile);
			if (AppUtil.compare(FileUtil.getFileName(aFile), mp3Tag.getTitle())) {
				return;
			}

			File renamedFile = FileUtil.renameFile(aFile, mp3Tag.getTitle());
			if (!renamedFile.exists()) {
				LOGGER.warn("Unable to rename track [{}] to [{}]", aFile, aAlbumTag.getTitle());
				msgBuffer.append(String.format("Unable to rename track [%s] to [%s]", aFile, aAlbumTag.getTitle()));
			}
		});

		/*
		 * rename album folder to <album>
		 */
		if (!AppUtil.compare(currentAlbumFolder.getName(), aAlbumTag.getAlbum())) {
			File renamedFile = FileUtil.renameFile(currentAlbumFolder, aAlbumTag.getAlbum());
			if (!renamedFile.exists()) {
				LOGGER.warn("Unable to rename album [{}] to [{}]", aAlbumTag.getFilePath(), aAlbumTag.getAlbum());
				msgBuffer.append(String.format("Unable to rename album [%s] to [%s]", aAlbumTag.getFilePath(),
						aAlbumTag.getAlbum()));
			} else {
				currentAlbumFolder = renamedFile;
			}
		}

		/*
		 * Move album folder to correct location under the library
		 */
		String albumPath = StringUtils.join(this.apolloProps.get("apollo.library.root"), UNIX_PATH_CHAR,
				aAlbumTag.getLanguage(), UNIX_PATH_CHAR, aAlbumTag.getGenre(), UNIX_PATH_CHAR,
				aAlbumTag.getAlbumArtist(), UNIX_PATH_CHAR, aAlbumTag.getAlbum());
		if (!AppUtil.compare(albumPath, currentAlbumPath)) {
			File destination = new File(albumPath).getParentFile();
			try {
				destination.mkdirs();
				FileUtils.moveDirectoryToDirectory(currentAlbumFolder, destination, true);
			} catch (IOException error) {
				LOGGER.warn("Unable to move album from [{}] to [{}]", aAlbumTag.getFilePath(), destination, error);
				msgBuffer.append(String.format("Unable to move album from [%s] to [%s] due to error [%s]",
						aAlbumTag.getFilePath(), destination, error.getMessage()));
			}
		}

		DataMap dataMap = new DataMap();
		dataMap.put("albumPath", albumPath);
		dataMap.put("msgBuffer", msgBuffer.toString());
		dataMap.put("resultFlags", (msgBuffer.length() == 0));
		return dataMap;
	}
}
