package apollo.service;

import static accelerate.util.AccelerateConstants.DOT_CHAR;
import static accelerate.util.AccelerateConstants.UNIX_PATH_CHAR;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
public class AlbumService {
	/**
	 * 
	 */
	private static Logger LOGGER = LoggerFactory.getLogger(AlbumService.class);

	/**
	 * 
	 */
	@Autowired
	private ApolloConfigProps apolloConfigProps = null;

	/**
	 * @param aAlbumPath
	 * @return
	 */
	@Auditable
	public DataMap getAlbumTags(String aAlbumPath) {
		final String fileExtn = this.apolloConfigProps.getFileExtn();
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

		String albumLibraryPath = StringUtils.join(this.apolloConfigProps.getLibraryRoot(), UNIX_PATH_CHAR,
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
	 * @param aAlbumPath
	 * @param aParseTagTokens
	 * @param aWriteFlag
	 * @return
	 */
	@Auditable
	public DataMap parseAlbumTags(String aAlbumPath, String aParseTagTokens, boolean aWriteFlag) {
		final String fileExtn = this.apolloConfigProps.getFileExtn();
		LOGGER.debug("Parsing tag tokens [{}] for tracks with extn [{}] under [{}]", aParseTagTokens, fileExtn,
				aAlbumPath);

		List<String> parseTokens = Mp3TagUtil.parseTagExpression(aParseTagTokens);

		Mp3Tag commonTag = new Mp3Tag();
		List<Mp3Tag> tagList = FileUtil.findFilesByExtn(aAlbumPath, fileExtn).parallelStream().map(aFile -> {
			Mp3Tag mp3Tag = new Mp3Tag(aFile, parseTokens);
			if (aWriteFlag) {
				mp3Tag.save();
			}

			commonTag.extractCommonTag(mp3Tag);
			return mp3Tag;
		}).collect(Collectors.toList());
		commonTag.clear();

		DataMap dataMap = new DataMap();
		dataMap.put("commonTag", commonTag);
		dataMap.put("trackTags", tagList);
		return dataMap;
	}

	/**
	 * @param aCommonTag
	 * @param aTargetPath
	 */
	@Auditable
	public void saveAlbumTag(Mp3Tag aCommonTag, String aTargetPath) {
		final String fileExtn = this.apolloConfigProps.getFileExtn();
		LOGGER.debug("Saving common tag for all tracks with extn [{}] under [{}]", fileExtn, aTargetPath);

		FileUtil.findFilesByExtn(aTargetPath, fileExtn).parallelStream().forEach(aFile -> {
			aCommonTag.save(0, aFile);
		});
		aCommonTag.clear();
	}

	/**
	 * @param aMp3TagList
	 */
	@SuppressWarnings("static-method")
	@Auditable
	public void saveTrackTags(List<Mp3Tag> aMp3TagList) {
		aMp3TagList.stream().parallel().forEach(aMp3Tag -> {
			LOGGER.debug("Saving tag [{}]", aMp3Tag);
			aMp3Tag.save();
		});
	}

	/**
	 * @param aAlbumTag
	 * @return
	 */
	@Auditable
	public DataMap correctFileNames(Mp3Tag aAlbumTag) {
		LOGGER.debug("Correcting files names for album [{}] at path [{}]", aAlbumTag.getAlbum(),
				aAlbumTag.getFilePath());

		DataMap dataMap = validateTagsBeforeFileChange(aAlbumTag);
		if (!dataMap.is("resultFlags")) {
			return dataMap;
		}

		Map<String, Mp3Tag> tagMap = dataMap.remove("tagMap");
		StringBuilder msgBuffer = new StringBuilder();
		String currentAlbumPath = aAlbumTag.getFilePath();
		File currentAlbumFolder = new File(currentAlbumPath);

		/*
		 * rename tracks to <title>.<extn>
		 */
		final String fileExtn = this.apolloConfigProps.getFileExtn();
		tagMap.values().parallelStream().forEach(aTrackTag -> {
			if (AppUtil.compare(aTrackTag.getFileName(), aTrackTag.getTitle())) {
				return;
			}

			File trackFile = new File(aTrackTag.getFilePath());
			File renamedFile = new File(trackFile.getParent(), aTrackTag.getTitle() + DOT_CHAR + fileExtn);

			try {
				FileUtils.moveFile(trackFile, renamedFile);
			} catch (IOException error) {
				LOGGER.warn("Unable to rename track [{}] to [{}]", trackFile, renamedFile, error);
				msgBuffer.append(String.format("Unable to rename track [%s] to [%s] due to error [%s]", trackFile,
						renamedFile, error.getMessage()));
			}
		});

		/*
		 * rename album folder to <album>
		 */
		if (!AppUtil.compare(currentAlbumFolder.getName(), aAlbumTag.getAlbum())) {
			File renamedFolder = new File(currentAlbumFolder.getParent(), aAlbumTag.getAlbum());

			try {
				FileUtils.moveDirectory(currentAlbumFolder, renamedFolder);
				currentAlbumFolder = renamedFolder;
				currentAlbumPath = FileUtil.getFilePath(currentAlbumFolder);
			} catch (IOException error) {
				LOGGER.warn("Unable to rename album folder [{}] to [{}]", currentAlbumFolder, renamedFolder, error);
				msgBuffer.append(String.format("Unable to rename album folder [%s] to [%s] due to error [%s]",
						currentAlbumFolder, renamedFolder, error.getMessage()));
			}
		}

		dataMap = new DataMap();
		dataMap.put("albumPath", currentAlbumPath);
		dataMap.put("msgBuffer", msgBuffer.toString());
		dataMap.put("resultFlags", (msgBuffer.length() == 0));
		return dataMap;
	}

	/**
	 * @param aAlbumTag
	 * @return
	 */
	@Auditable
	public DataMap addToLibrary(Mp3Tag aAlbumTag) {
		LOGGER.debug("Adding album [{}] at path [{}] to library", aAlbumTag.getAlbum(), aAlbumTag.getFilePath());

		DataMap dataMap = validateTagsBeforeFileChange(aAlbumTag);
		if (dataMap != null) {
			return dataMap;
		}

		StringBuilder msgBuffer = new StringBuilder();
		String currentAlbumPath = aAlbumTag.getFilePath();
		File currentAlbumFolder = new File(currentAlbumPath);

		/*
		 * Move album folder to correct location under the library
		 */
		String albumPath = StringUtils.join(this.apolloConfigProps.getLibraryRoot(), UNIX_PATH_CHAR,
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

		dataMap = new DataMap();
		dataMap.put("albumPath", albumPath);
		dataMap.put("msgBuffer", msgBuffer.toString());
		dataMap.put("resultFlags", (msgBuffer.length() == 0));
		return dataMap;
	}

	/**
	 * @param aAlbumTag
	 * @return
	 */
	@Auditable
	public DataMap validateTagsBeforeFileChange(Mp3Tag aAlbumTag) {
		final String fileExtn = this.apolloConfigProps.getFileExtn();
		LOGGER.debug("Adding album [{}] at path [{}] to library", aAlbumTag.getAlbum(), aAlbumTag.getFilePath());

		String currentAlbumPath = aAlbumTag.getFilePath();
		DataMap dataMap = new DataMap();
		dataMap.put("albumPath", currentAlbumPath);
		dataMap.put("resultFlags", false);

		/*
		 * Validate tags first
		 */
		if (AppUtil.isEmptyAny(aAlbumTag.getLanguage(), aAlbumTag.getGenre(), aAlbumTag.getAlbum(),
				aAlbumTag.getAlbumArtist())) {
			dataMap.put("msgBuffer", "Album tag is not complete");
			return dataMap;
		}

		Set<String> tagErrors = new HashSet<>();
		Map<String, Mp3Tag> tagMap = FileUtil.findFilesByExtn(currentAlbumPath, fileExtn).parallelStream()
				.map(aFile -> {
					Mp3Tag mp3Tag = new Mp3Tag(aFile);
					tagErrors.addAll(mp3Tag.getTagErrors());
					return mp3Tag;
				}).collect(Collectors.toMap(aTag -> aTag.getFilePath(), aTag -> aTag));

		if (!tagErrors.isEmpty()) {
			dataMap.put("msgBuffer", tagErrors);
			return dataMap;
		}

		dataMap.put("tagMap", tagMap);
		dataMap.put("resultFlags", true);
		return dataMap;
	}
}
