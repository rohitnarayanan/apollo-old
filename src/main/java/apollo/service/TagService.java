package apollo.service;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import accelerate.cache.PropertyCache;
import accelerate.databean.DataMap;
import accelerate.util.FileUtil;
import apollo.model.Mp3Tag;
import apollo.util.ID3Util;

/**
 * PUT DESCRIPTION HERE
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since 07-Jun-2016
 */
@Component
public class TagService {
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
	 * @param aDirPath
	 * @return
	 */
	public DataMap readTags(String aDirPath) {
		final boolean testMode = this.apolloProps.isEnabled("apollo.testMode");
		final String fileExtn = this.apolloProps.get("apollo.targetExtn");
		LOGGER.debug("Reading tags for all tracks with extn [{}] under [{}]", fileExtn, aDirPath);

		Mp3Tag commonTag = new Mp3Tag();
		DataMap dataMap = new DataMap();
		dataMap.put("commonTag", commonTag);
		dataMap.put("tags", FileUtil.findFilesByExtn(aDirPath, fileExtn).parallelStream().map(aFile -> {
			Mp3Tag mp3Tag = ID3Util.readTag(aFile, testMode);
			ID3Util.extractCommonTag(commonTag, mp3Tag);
			return mp3Tag;
		}).collect(Collectors.toList()));

		commonTag.clear();
		return dataMap;
	}
}
