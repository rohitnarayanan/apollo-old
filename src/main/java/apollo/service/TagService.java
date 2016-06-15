package apollo.service;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import accelerate.cache.PropertyCache;
import accelerate.databean.DataMap;
import accelerate.util.FileUtil;
import accelerate.util.JSONUtil;
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
	 * @param aTargetPath
	 * @return
	 */
	public DataMap readTags(String aTargetPath) {
		final boolean testMode = this.apolloProps.isEnabled("apollo.testMode");
		final String fileExtn = this.apolloProps.get("apollo.targetExtn");
		LOGGER.debug("Reading tags for all tracks with extn [{}] under [{}]", fileExtn, aTargetPath);

		Mp3Tag commonTag = new Mp3Tag();
		List<Mp3Tag> tagList = FileUtil.findFilesByExtn(aTargetPath, fileExtn).parallelStream().map(aFile -> {
			Mp3Tag mp3Tag = ID3Util.readTag(aFile, testMode);
			ID3Util.extractCommonTag(commonTag, mp3Tag);
			return mp3Tag;
		}).collect(Collectors.toList());
		commonTag.clear();

		DataMap dataMap = new DataMap();
		dataMap.put("commonTag", commonTag);
		dataMap.put("trackTags", tagList);
		return dataMap;
	}

	/**
	 * @param aTargetPath
	 * @param aParseTagTokens
	 * @return
	 */
	public DataMap parseTags(String aTargetPath, String aParseTagTokens) {
		final String fileExtn = this.apolloProps.get("apollo.targetExtn");
		LOGGER.debug("Parsing tag tokens [{}] for tracks with extn [{}] under [{}]", aParseTagTokens, fileExtn,
				aTargetPath);

		List<String> parseTokens = ID3Util.parseTagExpression(aParseTagTokens);

		Mp3Tag commonTag = new Mp3Tag();
		List<Mp3Tag> tagList = FileUtil.findFilesByExtn(aTargetPath, fileExtn).parallelStream().map(aFile -> {
			Mp3Tag mp3Tag = ID3Util.parseTag(aFile, parseTokens);
			ID3Util.extractCommonTag(commonTag, mp3Tag);
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
	public void saveCommonTag(Mp3Tag aCommonTag, String aTargetPath) {
		boolean testMode = this.apolloProps.isEnabled("apollo.testMode");
		String fileExtn = this.apolloProps.get("apollo.targetExtn");
		LOGGER.debug("Saving common tag for all tracks with extn [{}] under [{}]", fileExtn, aTargetPath);

		FileUtil.findFilesByExtn(aTargetPath, fileExtn).parallelStream().forEach(aFile -> {
			ID3Util.writeTag(aFile, aCommonTag, testMode);
		});
		aCommonTag.clear();
	}

	/**
	 * @param aMp3Tag
	 * @param aTargetFiles
	 */
	public void saveTag(Mp3Tag aMp3Tag, File... aTargetFiles) {
		boolean testMode = this.apolloProps.isEnabled("apollo.testMode");

		Arrays.stream(aTargetFiles).parallel().forEach(aTrack -> {
			LOGGER.debug("Saving common tag [{}] for track [{}]", aMp3Tag, aTrack);
			ID3Util.writeTag(aTrack, aMp3Tag, testMode);
		});
	}

	/**
	 * @param aMp3TagJSONList
	 */
	public void saveTags(String... aMp3TagJSONList) {
		boolean testMode = this.apolloProps.isEnabled("apollo.testMode");

		Arrays.stream(aMp3TagJSONList).parallel().forEach(aMp3TagJSON -> {
			LOGGER.debug("Saving tag [{}]", aMp3TagJSON);

			Mp3Tag mp3Tag = JSONUtil.deserialize(aMp3TagJSON, Mp3Tag.class);
			File track = new File(mp3Tag.getString("path"));
			ID3Util.writeTag(track, mp3Tag, testMode);
		});
	}
}
