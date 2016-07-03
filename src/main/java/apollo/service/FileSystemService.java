package apollo.service;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import accelerate.cache.PropertyCache;
import accelerate.databean.DataMap;
import accelerate.logging.Auditable;
import accelerate.util.FileUtil;

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
	 * @param aFoldersOnly
	 * @return
	 */
	@Auditable
	public DataMap getFileTree(String aDirPath, String aDirName, final Boolean aFoldersOnly) {
		String dirPath = StringUtils.isEmpty(aDirPath) ? this.apolloProps.get("apollo.fileselector.root") : aDirPath;
		File targetDir = StringUtils.isEmpty(aDirName) ? new File(dirPath) : new File(dirPath, aDirName);

		LOGGER.debug("Fetching file tree for [{}]", targetDir);

		List<DataMap> fileTree = Arrays.stream(targetDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File aFile) {
				return !aFile.getName().startsWith(".") && (!aFoldersOnly || aFile.isDirectory());
			}
		})).map(aFile -> {
			boolean children = false;
			if (aFile.isDirectory()) {
				if (!aFoldersOnly) {
					children = !ObjectUtils.isEmpty(aFile.listFiles());
				} else {
					children = !ObjectUtils.isEmpty(aFile.listFiles(new FileFilter() {
						@Override
						public boolean accept(File aInnerFile) {
							return aInnerFile.isDirectory();
						}
					}));
				}
			}

			return DataMap.buildMap("text", aFile.getName(), "data", targetDir.getPath(), "children", children);
		}).collect(Collectors.toList());

		DataMap dataMap = new DataMap();
		dataMap.put("path", FileUtil.getFilePath(targetDir));
		dataMap.put("fileTree", fileTree);

		return dataMap;
	}
}
