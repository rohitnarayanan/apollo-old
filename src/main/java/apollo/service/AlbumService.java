package apollo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import accelerate.utils.CommonUtils;
import accelerate.utils.NIOUtil;
import accelerate.utils.bean.DataMap;
import accelerate.utils.logging.Log;
import apollo.config.ApolloConfigProps;
import apollo.model.Mp3Tag;
import apollo.util.Mp3TagUtil;

/**
 * Service class for Album operations
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since December 11, 2017
 */
@Component
public class AlbumService {
	/**
	 * {@link Logger} instance
	 */
	private static Logger _LOGGER = LoggerFactory.getLogger(AlbumService.class);

	/**
	 * {@link ApolloConfigProps} instance
	 */
	@Autowired
	private ApolloConfigProps apolloConfigProps = null;

	/**
	 * @param aAlbumPath
	 * @return
	 */
	@Log
	public DataMap getAlbumTags(Path aAlbumPath) {
		final String fileExtn = this.apolloConfigProps.getFileExtn();
		_LOGGER.debug("Reading tags for all tracks with extn [{}] under [{}]", fileExtn, aAlbumPath);

		Mp3Tag albumTag = new Mp3Tag();
		List<Mp3Tag> songTags = NIOUtil.searchByExtn(aAlbumPath, fileExtn).values().parallelStream().map(aPath -> {
			Mp3Tag mp3Tag = new Mp3Tag(aPath);
			albumTag.extractCommonTag(mp3Tag);

			/*
			 * Removing tag values set at album level to reduce data transmission
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

		albumTag.setFilePath(aAlbumPath);
		albumTag.clear();

		Path albumLibraryPath = Paths.get(this.apolloConfigProps.getLibraryRoot(), albumTag.getGenre(),
				albumTag.getAlbumArtist(), albumTag.getAlbum());
		boolean addedToLibrary = CommonUtils.compare(albumLibraryPath, aAlbumPath);

		DataMap dataMap = new DataMap();
		dataMap.put("albumTag", albumTag);
		dataMap.put("songTags", songTags);
		dataMap.put("addedToLibrary", addedToLibrary);
		return dataMap;
	}

	/**
	 * @param aAlbumPath
	 * @param aParseTagTokens
	 * @param aWriteFlag
	 * @return
	 */
	@Log
	public DataMap parseAlbumTags(Path aAlbumPath, String aParseTagTokens, boolean aWriteFlag) {
		final String fileExtn = this.apolloConfigProps.getFileExtn();
		_LOGGER.debug("Parsing tag tokens [{}] for tracks with extn [{}] under [{}]", aParseTagTokens, fileExtn,
				aAlbumPath);

		List<String> parseTokens = Mp3TagUtil.parseTagExpression(aParseTagTokens);

		Mp3Tag albumTag = new Mp3Tag();
		List<Mp3Tag> songTags = NIOUtil.searchByExtn(aAlbumPath, fileExtn).values().parallelStream().map(aPath -> {
			Mp3Tag mp3Tag = new Mp3Tag(aPath, parseTokens);
			if (aWriteFlag) {
				mp3Tag.save();
			}

			albumTag.extractCommonTag(mp3Tag);
			return mp3Tag;
		}).collect(Collectors.toList());
		albumTag.clear();

		DataMap dataMap = new DataMap();
		dataMap.put("albumTag", albumTag);
		dataMap.put("songTags", songTags);
		return dataMap;
	}

	/**
	 * @param aAlbumTag
	 */
	@Log
	public void saveAlbumTag(Mp3Tag aAlbumTag) {
		final String fileExtn = this.apolloConfigProps.getFileExtn();
		_LOGGER.debug("Saving common tag for all tracks with extn [{}] under [{}]", fileExtn, aAlbumTag.getFilePath());

		NIOUtil.searchByExtn(aAlbumTag.getFilePath(), fileExtn).values().parallelStream().forEach(aSongPath -> {
			aAlbumTag.save(0, aSongPath);
		});
		aAlbumTag.clear();
	}

	/**
	 * @param aMp3TagList
	 */
	@SuppressWarnings("static-method")
	@Log
	public void saveAlbumTags(List<Mp3Tag> aMp3TagList) {
		aMp3TagList.stream().parallel().forEach(aMp3Tag -> {
			_LOGGER.debug("Saving tag [{}]", aMp3Tag);
			aMp3Tag.save();
		});
	}

	/**
	 * @param aAlbumTag
	 * @return
	 */
	@Log
	public DataMap renameAlbumSongs(Mp3Tag aAlbumTag) {
		_LOGGER.debug("Correcting files names for album [{}] at path [{}]", aAlbumTag.getAlbum(),
				aAlbumTag.getFilePath());

		DataMap dataMap = validateTagsBeforeFileChange(aAlbumTag);
		if (!dataMap.is("resultFlags")) {
			return dataMap;
		}

		Map<String, Mp3Tag> tagMap = dataMap.remove("tagMap");
		StringBuilder msgBuffer = new StringBuilder();

		/*
		 * rename tracks to <title>.<extension>
		 */
		tagMap.values().parallelStream().forEach(aTrackTag -> {
			if (CommonUtils.compare(aTrackTag.getFileName(), aTrackTag.getTitle())) {
				return;
			}

			Path trackPath = aTrackTag.getFilePath();

			try {
				NIOUtil.rename(trackPath, aTrackTag.getTitle());
			} catch (IOException error) {
				_LOGGER.error("Unable to rename track [{}] to [{}]", trackPath, aTrackTag.getTitle(), error);
				msgBuffer.append(String.format("Unable to rename track [%s] to [%s] due to error [%s]", trackPath,
						aTrackTag.getTitle(), error.getMessage()));
			}
		});

		dataMap = new DataMap();
		dataMap.put("albumPath", aAlbumTag.getFilePath());
		dataMap.put("msgBuffer", msgBuffer.toString());
		dataMap.put("resultFlags", (msgBuffer.length() == 0));
		return dataMap;
	}

	/**
	 * @param aAlbumTag
	 * @return
	 */
	@Log
	public DataMap addToLibrary(Mp3Tag aAlbumTag) {
		_LOGGER.debug("Adding album [{}] at path [{}] to library", aAlbumTag.getAlbum(), aAlbumTag.getFilePath());

		DataMap dataMap = validateTagsBeforeFileChange(aAlbumTag);
		if (!dataMap.is("resultFlags")) {
			return dataMap;
		}

		StringBuilder msgBuffer = new StringBuilder();
		Path currentAlbumPath = aAlbumTag.getFilePath();

		/*
		 * Move album folder to correct location under the library
		 */
		Path albumPath = Paths.get(this.apolloConfigProps.getLibraryRoot(), aAlbumTag.getGenre(),
				aAlbumTag.getAlbumArtist(), aAlbumTag.getAlbum());
		if (!CommonUtils.compare(albumPath, currentAlbumPath)) {
			try {
				Files.createDirectories(albumPath.getParent());
				Files.move(currentAlbumPath, albumPath, StandardCopyOption.ATOMIC_MOVE);
			} catch (IOException error) {
				_LOGGER.warn("Unable to move album from [{}] to [{}]", currentAlbumPath, albumPath, error);
				msgBuffer.append(String.format("Unable to move album from [%s] to [%s] due to error [%s]",
						currentAlbumPath, albumPath, error.getMessage()));
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
	@Log
	public DataMap validateTagsBeforeFileChange(Mp3Tag aAlbumTag) {
		final String fileExtn = this.apolloConfigProps.getFileExtn();
		_LOGGER.debug("Adding album [{}] at path [{}] to library", aAlbumTag.getAlbum(), aAlbumTag.getFilePath());

		Path currentAlbumPath = aAlbumTag.getFilePath();
		DataMap dataMap = new DataMap();
		dataMap.put("albumPath", currentAlbumPath);
		dataMap.put("resultFlags", false);

		/*
		 * Validate tags first
		 */
		if (CommonUtils.isEmptyAny(aAlbumTag.getLanguage(), aAlbumTag.getGenre(), aAlbumTag.getAlbum(),
				aAlbumTag.getAlbumArtist())) {
			dataMap.put("msgBuffer", "Album tag is not complete");
			return dataMap;
		}

		Set<String> tagErrors = new HashSet<>();
		Map<Path, Mp3Tag> tagMap = NIOUtil.searchByExtn(currentAlbumPath, fileExtn).values().parallelStream()
				.map(aPath -> {
					Mp3Tag mp3Tag = new Mp3Tag(aPath);
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
