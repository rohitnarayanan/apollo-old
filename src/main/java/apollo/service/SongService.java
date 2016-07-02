package apollo.service;

import static accelerate.util.AccelerateConstants.UNIX_PATH_CHAR;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import accelerate.cache.PropertyCache;
import accelerate.databean.DataMap;
import accelerate.logging.Auditable;
import accelerate.util.AppUtil;
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
	private PropertyCache apolloProps = null;

	/**
	 * @param aSongPath
	 * @return
	 */
	@Auditable
	public DataMap getTag(String aSongPath) {
		LOGGER.debug("Reading tag [{}]", aSongPath);
		Mp3Tag songTag = new Mp3Tag(new File(aSongPath));
		String songLibraryPath = StringUtils.join(this.apolloProps.get("apollo.library.root"), UNIX_PATH_CHAR,
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
		final String fileExtn = this.apolloProps.get("apollo.targetExtn");
		LOGGER.debug("Parsing tag tokens [{}] for tracks with extn [{}] under [{}]", aParseTagTokens, fileExtn,
				aSongPath);

		List<String> parseTokens = Mp3TagUtil.parseTagExpression(aParseTagTokens);
		Mp3Tag songTag = new Mp3Tag(new File(aSongPath), parseTokens);
		if (aWriteFlag) {
			songTag.save();
		}

		return songTag;
	}
}
