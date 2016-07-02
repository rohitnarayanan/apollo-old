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
import apollo.util.Mp3TagUtil;

/**
 * PUT DESCRIPTION HERE
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since 07-Jun-2016
 */
@SuppressWarnings("static-method")
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
	 * @param aMp3Tag
	 */
	@Auditable
	public void saveTag2(Mp3Tag aMp3Tag) {
		LOGGER.debug("Saving tag [{}]", aMp3Tag);
		aMp3Tag.save();
	}

	/**
	 * @param aMp3TagList
	 */
	@Auditable
	public void saveTags2(List<Mp3Tag> aMp3TagList) {
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
	public void saveCommonTag2(Mp3Tag aCommonTag, String aTargetPath) {
		String fileExtn = this.apolloProps.get("apollo.targetExtn");
		LOGGER.debug("Saving common tag for all tracks with extn [{}] under [{}]", fileExtn, aTargetPath);

		FileUtil.findFilesByExtn(aTargetPath, fileExtn).parallelStream().forEach(aFile -> {
			aCommonTag.save(0, aFile);
		});
		aCommonTag.clear();
	}
}
