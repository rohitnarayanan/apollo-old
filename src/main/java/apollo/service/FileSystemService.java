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

import accelerate.databean.DataMap;
import accelerate.logging.Auditable;
import accelerate.util.AppUtil;
import accelerate.util.FileUtil;
import apollo.config.ApolloConfigProps;

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
	private ApolloConfigProps apolloConfigProps = null;

	/**
	 * @param aDirPath
	 * @param aDirName
	 * @param aFileType
	 *            - 'all' for all files, 'none' for folders only, 'xyz' to match
	 *            file extension
	 * @return
	 */
	@Auditable
	public DataMap getFileTree(String aDirPath, String aDirName, final String aFileType) {
		String dirPath = StringUtils.isEmpty(aDirPath) ? this.apolloConfigProps.getFileSelectorRoot() : aDirPath;
		File targetDir = StringUtils.isEmpty(aDirName) ? new File(dirPath) : new File(dirPath, aDirName);

		LOGGER.debug("Fetching file tree for [{}]", targetDir);

		List<DataMap> fileTree = Arrays.stream(targetDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File aFile) {
				if (aFile.getName().startsWith(".")) {
					return false;
				}

				if (AppUtil.compare(aFileType, "all")) {
					return true;
				} else if (AppUtil.compare(aFileType, "none")) {
					return aFile.isDirectory();
				}

				return aFile.isDirectory() || AppUtil.compare(aFileType, FileUtil.getFileExtn(aFile));
			}
		})).map(aFile -> {
			boolean children = false;
			if (aFile.isDirectory()) {
				if (AppUtil.compare(aFileType, "all")) {
					children = !ObjectUtils.isEmpty(aFile.listFiles());
				} else {
					children = !ObjectUtils.isEmpty(aFile.listFiles(new FileFilter() {
						@Override
						public boolean accept(File aInnerFile) {
							return aInnerFile.isDirectory() || (AppUtil.compare(aFileType, "none") ? false
									: AppUtil.compare(aFileType, FileUtil.getFileExtn(aInnerFile)));
						}
					}));
				}
			}

			return DataMap.buildMap("text", aFile.getName(), "data",
					DataMap.buildMap("path", FileUtil.getFilePath(targetDir), "type",
							(aFile.isDirectory()) ? "folder" : FileUtil.getFileExtn(aFile)),
					"children", children, "icon", (aFile.isDirectory()) ? null : "fa fa-file-audio-o");
		}).collect(Collectors.toList());

		DataMap dataMap = new DataMap();
		dataMap.put("path", FileUtil.getFilePath(targetDir));
		dataMap.put("fileTree", fileTree);

		return dataMap;
	}
}
