package apollo.service;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import accelerate.cache.PropertyCache;
import accelerate.databean.DataMap;
import accelerate.logging.Auditable;
import accelerate.util.AppUtil;
import accelerate.util.FileUtil;
import apollo.util.ID3Util;

/**
 * PUT DESCRIPTION HERE
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since 07-Jun-2016
 */
@Component
public class FileSystemService {
	/**
	 * 
	 */
	private static Logger LOGGER = LoggerFactory.getLogger(FileSystemService.class);

	/**
	 * 
	 */
	@Autowired
	private PropertyCache apolloProps = null;

	/**
	 * @param aDirPath
	 * @param aDirName
	 * @return
	 */
	@Auditable
	public DataMap listFolders(String aDirPath, String aDirName) {
		String dirPath = StringUtils.isEmpty(aDirPath) ? this.apolloProps.get("apollo.library.root") : aDirPath;
		File targetDir = StringUtils.isEmpty(aDirName) ? new File(dirPath) : new File(dirPath, aDirName);

		LOGGER.debug("Fetching sub folders for [{}]", targetDir);

		DataMap dataMap = new DataMap();
		dataMap.put("path", StringUtils.replacePattern(targetDir.getPath(), "\\\\", "/"));
		dataMap.put("folders", Arrays.stream(targetDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File aFile) {
				return !aFile.getName().startsWith(".") && aFile.isDirectory();
			}
		})).map(aFile -> {
			File[] childFolders = aFile.listFiles(new FileFilter() {
				@Override
				public boolean accept(File aInnerFile) {
					return aInnerFile.isDirectory();
				}
			});

			return DataMap.buildMap("text", aFile.getName(), "data", targetDir.getPath(), "children",
					!ObjectUtils.isEmpty(childFolders));
		}).collect(Collectors.toList()));

		return dataMap;
	}

	/**
	 * @param aDirPath
	 * @return
	 */
	public static DataMap listTracks(String aDirPath) {
		File targetDir = new File(aDirPath);
		Assert.isTrue(targetDir.isDirectory(), "Directory path should be a valid folder");

		LOGGER.debug("Fetching tracks for [{}]", targetDir);

		DataMap dataMap = new DataMap();
		dataMap.put("path", targetDir.getPath());
		dataMap.put("tracks", Arrays.stream(targetDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File aFile) {
				return AppUtil.compare(FileUtil.getFileExtn(aFile), "mp3");
			}
		})).map(aFile ->
		// ID3Util.tempTag(aFile)
		ID3Util.readTag(aFile)).collect(Collectors.toList()));

		return dataMap;
	}
}
