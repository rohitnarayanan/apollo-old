package apollo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import accelerate.cache.PropertyCache;
import accelerate.databean.DataMap;
import accelerate.logging.Auditable;
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
		albumTag.clear();

		DataMap dataMap = new DataMap();
		dataMap.put("albumTag", albumTag);
		dataMap.put("trackTags", tagList);
		return dataMap;
	}
}
