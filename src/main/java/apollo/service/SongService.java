package apollo.service;

import static accelerate.utils.CommonConstants.DOT_CHAR;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
 * Service to handle Song operations
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since December 11, 2017
 */
@Component
public class SongService {
	/**
	 * {@link Logger} instance
	 */
	private static Logger _LOGGER = LoggerFactory.getLogger(SongService.class);

	/**
	 * {@link ApolloConfigProps} instance
	 */
	@Autowired
	private ApolloConfigProps apolloConfigProps = null;

	/**
	 * @param aSongPath
	 * @return
	 */
	@Log
	public DataMap getTag(Path aSongPath) {
		_LOGGER.debug("Reading tag [{}]", aSongPath);
		Mp3Tag mp3Tag = new Mp3Tag(aSongPath);
		Path songLibraryPath = Paths.get(this.apolloConfigProps.getLibraryRoot(), mp3Tag.getGenre(),
				mp3Tag.getAlbumArtist(), mp3Tag.getAlbum(),
				mp3Tag.getTitle() + DOT_CHAR + this.apolloConfigProps.getFileExtn());
		boolean addedToLibrary = songLibraryPath.equals(aSongPath);

		DataMap dataMap = new DataMap();
		dataMap.put("tag", mp3Tag);
		dataMap.put("addedToLibrary", addedToLibrary);
		return dataMap;
	}

	/**
	 * @param aSongPath
	 * @param aParseTagTokens
	 * @param aWriteFlag
	 * @return
	 */
	@Log
	public Mp3Tag parseTags(Path aSongPath, String aParseTagTokens, boolean aWriteFlag) {
		final String fileExtn = this.apolloConfigProps.getFileExtn();
		_LOGGER.debug("Parsing tag tokens [{}] for songs with extn [{}] under [{}]", aParseTagTokens, fileExtn,
				aSongPath);

		List<String> parseTokens = Mp3TagUtil.parseTagExpression(aParseTagTokens);
		Mp3Tag mp3Tag = new Mp3Tag(aSongPath, parseTokens);
		if (aWriteFlag) {
			mp3Tag.save();
		}

		return mp3Tag;
	}

	/**
	 * @param aMp3Tag
	 */
	@SuppressWarnings("static-method")
	@Log
	public void saveTag(Mp3Tag aMp3Tag) {
		_LOGGER.debug("Saving tag [{}]", aMp3Tag);
		aMp3Tag.save();
	}

	/**
	 * @param aSongPath
	 * @return
	 * @throws IOException
	 */
	@Log
	public DataMap addToLibrary(Path aSongPath) throws IOException {
		_LOGGER.debug("Adding song [{}] to library", aSongPath);

		Mp3Tag mp3Tag = new Mp3Tag(aSongPath);
		if (!mp3Tag.getTagErrors().isEmpty()) {
			DataMap dataMap = new DataMap();
			dataMap.put("path", NIOUtil.getPathString(aSongPath));
			dataMap.put("msgBuffer", mp3Tag.getTagErrors());
			dataMap.put("resultFlag", false);
			return dataMap;
		}

		StringBuilder msgBuffer = new StringBuilder();
		Path currentPath = mp3Tag.getFilePath();

		/*
		 * rename song to <title>.<extension>
		 */
		if (!CommonUtils.compare(NIOUtil.getBaseName(currentPath), mp3Tag.getTitle())) {
			Path renamedPath = NIOUtil.rename(currentPath, mp3Tag.getTitle());
			if (!Files.exists(renamedPath)) {
				_LOGGER.warn("Unable to rename file [{}] to [{}]", currentPath, mp3Tag.getTitle());
				msgBuffer.append(String.format("Unable to rename song [%s] to [%s]", currentPath, mp3Tag.getTitle()));
			} else {
				currentPath = renamedPath;
			}
		}

		/*
		 * Move song to correct location under the library
		 */
		Path songPath = Paths.get(this.apolloConfigProps.getLibraryRoot(), mp3Tag.getGenre(), mp3Tag.getAlbumArtist(),
				mp3Tag.getAlbum(), mp3Tag.getTitle() + DOT_CHAR + this.apolloConfigProps.getFileExtn());
		if (!CommonUtils.compare(songPath, currentPath)) {
			try {
				Files.move(currentPath, songPath);
			} catch (IOException error) {
				_LOGGER.error("Unable to move song from [{}] to [{}]", currentPath, songPath, error);
				msgBuffer.append(String.format("Unable to move album from [%s] to [%s] due to error [%s]", currentPath,
						songPath, error.getMessage()));
			}
		}

		DataMap dataMap = new DataMap();
		dataMap.put("songPath", songPath);
		dataMap.put("msgBuffer", msgBuffer.toString());
		dataMap.put("resultFlag", (msgBuffer.length() == 0));
		return dataMap;
	}
}