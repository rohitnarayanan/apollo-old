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
import accelerate.logging.Auditable;
import accelerate.util.FileUtil;
import accelerate.util.JSONUtil;
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
	 * @param aParseTagTokens
	 * @param aWriteFlag
	 * @return
	 */
	@Auditable
	public DataMap parseTags(String aTargetPath, String aParseTagTokens, boolean aWriteFlag) {
		final String fileExtn = this.apolloProps.get("apollo.targetExtn");
		LOGGER.debug("Parsing tag tokens [{}] for tracks with extn [{}] under [{}]", aParseTagTokens, fileExtn,
				aTargetPath);

		List<String> parseTokens = Mp3TagUtil.parseTagExpression(aParseTagTokens);

		Mp3Tag commonTag = new Mp3Tag();
		List<Mp3Tag> tagList = FileUtil.findFilesByExtn(aTargetPath, fileExtn).parallelStream().map(aFile -> {
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
	 * @param aMp3Tag
	 * @param aTargetFiles
	 */
	@SuppressWarnings("static-method")
	@Auditable
	public void saveTag(Mp3Tag aMp3Tag, File... aTargetFiles) {
		Arrays.stream(aTargetFiles).parallel().forEach(aTrack -> {
			LOGGER.debug("Saving common tag [{}] for track [{}]", aMp3Tag, aTrack);
			aMp3Tag.writeTo(aTrack);
		});
	}

	/**
	 * @param aMp3TagJSONList
	 */
	@SuppressWarnings("static-method")
	@Auditable
	public void saveTags(String... aMp3TagJSONList) {
		Arrays.stream(aMp3TagJSONList).parallel().forEach(aMp3TagJSON -> {
			LOGGER.debug("Saving tag [{}]", aMp3TagJSON);

			Mp3Tag mp3Tag = JSONUtil.deserialize(aMp3TagJSON, Mp3Tag.class);
			mp3Tag.save();
		});
	}

	/**
	 * @param aMp3TagList
	 */
	@SuppressWarnings("static-method")
	@Auditable
	public void saveTags(List<Mp3Tag> aMp3TagList) {
		aMp3TagList.stream().parallel().forEach(aMp3Tag -> {
			LOGGER.debug("Saving tag [{}]", aMp3Tag);
			aMp3Tag.save();
		});
	}

	/**
	 * @param aCommonTag
	 * @param aTargetPath
	 */
	@Auditable
	public void saveCommonTag(Mp3Tag aCommonTag, String aTargetPath) {
		String fileExtn = this.apolloProps.get("apollo.targetExtn");
		LOGGER.debug("Saving common tag for all tracks with extn [{}] under [{}]", fileExtn, aTargetPath);

		FileUtil.findFilesByExtn(aTargetPath, fileExtn).parallelStream().forEach(aFile -> {
			aCommonTag.writeTo(aFile);
		});
		aCommonTag.clear();
	}
}
