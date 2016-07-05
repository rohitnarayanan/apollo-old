package apollo.service;

import static accelerate.util.AccelerateConstants.DOT_CHAR;
import static accelerate.util.AccelerateConstants.UNIX_PATH_CHAR;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import accelerate.databean.DataMap;
import accelerate.logging.Auditable;
import accelerate.util.AppUtil;
import accelerate.util.FileUtil;
import apollo.config.ApolloConfigProps;
import apollo.model.Mp3Tag;
import apollo.util.Mp3TagUtil;

/**
 * PUT DESCRIPTION HERE
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since 07-Jun-2016
 */
@Component
public class SongService {
	/**
	 * 
	 */
	private static Logger LOGGER = LoggerFactory.getLogger(SongService.class);

	/**
	 * 
	 */
	@Autowired
	private ApolloConfigProps apolloConfigProps = null;

	/**
	 * @param aSongPath
	 * @return
	 */
	@Auditable
	public DataMap getTag(String aSongPath) {
		LOGGER.debug("Reading tag [{}]", aSongPath);
		Mp3Tag songTag = new Mp3Tag(new File(aSongPath));
		String songLibraryPath = StringUtils.join(this.apolloConfigProps.getLibraryRoot(), UNIX_PATH_CHAR,
				songTag.getLanguage(), UNIX_PATH_CHAR, songTag.getGenre(), UNIX_PATH_CHAR, songTag.getAlbumArtist(),
				UNIX_PATH_CHAR, songTag.getAlbum());
		boolean addedToLibrary = AppUtil.compare(songLibraryPath, aSongPath);

		DataMap dataMap = new DataMap();
		dataMap.put("songTag", songTag);
		dataMap.put("addedToLibrary", addedToLibrary);
		return dataMap;
	}

	/**
	 * @param aSongPath
	 * @param aParseTagTokens
	 * @param aWriteFlag
	 * @return
	 */
	@Auditable
	public Mp3Tag parseSongTags(String aSongPath, String aParseTagTokens, boolean aWriteFlag) {
		final String fileExtn = this.apolloConfigProps.getFileExtn();
		LOGGER.debug("Parsing tag tokens [{}] for tracks with extn [{}] under [{}]", aParseTagTokens, fileExtn,
				aSongPath);

		List<String> parseTokens = Mp3TagUtil.parseTagExpression(aParseTagTokens);
		Mp3Tag songTag = new Mp3Tag(new File(aSongPath), parseTokens);
		if (aWriteFlag) {
			songTag.save();
		}

		return songTag;
	}

	/**
	 * @param aMp3Tag
	 */
	@SuppressWarnings("static-method")
	@Auditable
	public void saveSongTag(Mp3Tag aMp3Tag) {
		LOGGER.debug("Saving tag [{}]", aMp3Tag);
		aMp3Tag.save();
	}

	/**
	 * @param aSongPath
	 * @return
	 */
	@Auditable
	public DataMap addToLibrary(String aSongPath) {
		final String fileExtn = this.apolloConfigProps.getFileExtn();
		LOGGER.debug("Adding song [{}] to library", aSongPath);

		Mp3Tag songTag = new Mp3Tag(new File(aSongPath));
		if (!songTag.getTagErrors().isEmpty()) {
			DataMap dataMap = new DataMap();
			dataMap.put("songPath", aSongPath);
			dataMap.put("msgBuffer", songTag.getTagErrors());
			dataMap.put("resultFlag", false);
			return dataMap;
		}

		StringBuilder msgBuffer = new StringBuilder();
		String currentSongPath = songTag.getFilePath();
		File currentSongFile = new File(currentSongPath);

		/*
		 * rename track to <title>.<extn>
		 */
		if (!AppUtil.compare(FileUtil.getFileName(currentSongFile), songTag.getTitle())) {
			File renamedFile = FileUtil.renameFile(currentSongFile, songTag.getTitle());
			if (!renamedFile.exists()) {
				LOGGER.warn("Unable to rename song [{}] to [{}]", currentSongFile, songTag.getTitle());
				msgBuffer.append(
						String.format("Unable to rename track [%s] to [%s]", currentSongFile, songTag.getTitle()));
			} else {
				currentSongFile = renamedFile;
				currentSongPath = FileUtil.getFilePath(currentSongFile);
			}
		}

		/*
		 * Move song to correct location under the library
		 */
		String songPath = StringUtils.join(this.apolloConfigProps.getLibraryRoot(), UNIX_PATH_CHAR,
				songTag.getLanguage(), UNIX_PATH_CHAR, songTag.getGenre(), UNIX_PATH_CHAR, songTag.getAlbumArtist(),
				UNIX_PATH_CHAR, songTag.getAlbum(), UNIX_PATH_CHAR, songTag.getTitle(), DOT_CHAR, fileExtn);
		if (!AppUtil.compare(songPath, currentSongPath)) {
			File destination = new File(songPath).getParentFile();
			try {
				destination.mkdirs();
				FileUtils.moveFileToDirectory(currentSongFile, destination, false);
			} catch (IOException error) {
				LOGGER.warn("Unable to move album from [{}] to [{}]", songTag.getFilePath(), destination, error);
				msgBuffer.append(String.format("Unable to move album from [%s] to [%s] due to error [%s]",
						songTag.getFilePath(), destination, error.getMessage()));
			}
		}

		DataMap dataMap = new DataMap();
		dataMap.put("songPath", songPath);
		dataMap.put("msgBuffer", msgBuffer.toString());
		dataMap.put("resultFlag", (msgBuffer.length() == 0));
		return dataMap;
	}
}