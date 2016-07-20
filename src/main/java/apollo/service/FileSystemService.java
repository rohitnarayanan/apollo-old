package apollo.service;

import static accelerate.util.AccelerateConstants.COMMA_CHAR;
import static accelerate.util.AccelerateConstants.HYPHEN_CHAR;
import static accelerate.util.AccelerateConstants.SEMICOLON_CHAR;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import accelerate.databean.DataMap;
import accelerate.exception.AccelerateException;
import accelerate.logging.Auditable;
import accelerate.util.AppUtil;
import accelerate.util.FileUtil;
import accelerate.util.file.DirectoryParser;
import accelerate.util.file.DirectoryParser.FileHandler;
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

		DataMap dataMap = new DataMap();
		dataMap.put("path", FileUtil.getFilePath(targetDir));

		if (!targetDir.isDirectory()) {
			dataMap.put("message", "Root not a valid directory");
			return dataMap;
		}

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

		dataMap.put("fileTree", fileTree);
		return dataMap;
	}

	/**
	 * @param aInputParams
	 * @return
	 */
	@Auditable
	@SuppressWarnings("static-method")
	public DataMap compareFolders(Map<String, String> aInputParams) {
		/*
		 * Validate Input
		 */
		DataMap dataMap = new DataMap();
		StringBuilder message = new StringBuilder();
		File sourceFolder = new File(aInputParams.getOrDefault("sourcePath", "/INVALID_FOLDER_ROOT"));
		File targetFolder = new File(aInputParams.getOrDefault("targetPath", "/INVALID_FOLDER_ROOT"));

		if (!sourceFolder.exists() || !sourceFolder.isDirectory()) {
			message.append("Source is not a valid directory !!");
			message.append(HYPHEN_CHAR);
			message.append(aInputParams.get("sourcePath"));
			dataMap.put("errorFlag", true);
		}

		if (!targetFolder.exists() || !targetFolder.isDirectory()) {
			message.append("Target is not a valid directory !!");
			message.append(HYPHEN_CHAR);
			message.append(aInputParams.get("targetPath"));
			dataMap.put("errorFlag", true);
		}

		if (dataMap.is("errorFlag")) {
			dataMap.put("message", message.toString());
			return dataMap;
		}

		/*
		 * Prepare variables
		 */
		String[] ignoreExtensions = StringUtils.split(aInputParams.getOrDefault("ignoreExtnList", ""), COMMA_CHAR);

		final int sourcePathLength = FileUtil.getFilePath(sourceFolder).length();
		final int targetPathLength = FileUtil.getFilePath(targetFolder).length();
		final int[] handlerMode = new int[] { 0 };
		final Map<String, File> sourceFilesMap = new HashMap<>();
		final Map<String, File> targetFilesMap = new HashMap<>();

		FileHandler fileHandler = new FileHandler() {
			@Override
			public File handleDirectory(File aFolder) throws AccelerateException {
				int pathIndex = (handlerMode[0] == 0) ? sourcePathLength : targetPathLength;
				File checkRoot = (handlerMode[0] == 0) ? targetFolder : sourceFolder;
				Map<String, File> filesMap = (handlerMode[0] == 0) ? sourceFilesMap : targetFilesMap;

				String shortPath = FileUtil.getFilePath(aFolder).substring(pathIndex);
				File chkFolder = new File(checkRoot, shortPath);
				if (!chkFolder.exists()) {
					filesMap.put(shortPath, aFolder);
					return null;
				}

				return aFolder;
			}

			@Override
			public File handleFile(File aFile) throws AccelerateException {
				int pathIndex = (handlerMode[0] == 0) ? sourcePathLength : targetPathLength;
				Map<String, File> filesMap = (handlerMode[0] == 0) ? sourceFilesMap : targetFilesMap;

				filesMap.put(FileUtil.getFilePath(aFile).substring(pathIndex), aFile);
				return aFile;
			}
		};

		DirectoryParser.execute(sourceFolder,
				aFile -> !AppUtil.compareAny(accelerate.util.FileUtil.getFileExtn(aFile), ignoreExtensions),
				fileHandler);

		// flip the mode
		handlerMode[0] = 1;
		DirectoryParser.execute(targetFolder,
				aFile -> !AppUtil.compareAny(accelerate.util.FileUtil.getFileExtn(aFile), ignoreExtensions),
				fileHandler);

		List<DataMap> missingInTarget = new ArrayList<>();
		List<DataMap> missingInSource = new ArrayList<>();
		List<DataMap> conflictingFiles = new ArrayList<>();

		for (Entry<String, File> entry : sourceFilesMap.entrySet()) {
			File sourceFile = entry.getValue();
			File targetFile = targetFilesMap.get(entry.getKey());
			if (targetFile == null) {
				missingInTarget.add(DataMap.buildMap("path", FileUtil.getFilePath(sourceFile), "key", entry.getKey(),
						"name", sourceFile.getName(), "type", sourceFile.isFile() ? "file" : "directory", "size",
						sourceFile.length(), "lastModified",
						DateFormatUtils.format(new Date(sourceFile.lastModified()), "MMM dd yy, HH:mm:SS")));
			} else if (sourceFile.length() == targetFile.length()) {
				continue;
			} else {
				// conflicted files
				conflictingFiles.add(DataMap.buildMap("key", entry.getKey(), "type",
						sourceFile.isFile() ? "file" : "directory", "source",
						DataMap.buildMap("path", FileUtil.getFilePath(sourceFile), "name", sourceFile.getName(), "size",
								sourceFile.length(), "lastModified",
								DateFormatUtils.format(new Date(sourceFile.lastModified()), "MMM dd yy, HH:mm:SS")),
						"target",
						DataMap.buildMap("path", FileUtil.getFilePath(sourceFile), "name", sourceFile.getName(), "size",
								sourceFile.length(), "lastModified", DateFormatUtils
										.format(new Date(sourceFile.lastModified()), "MMM dd yy, HH:mm:SS"))));
			}
		}

		for (Entry<String, File> entry : targetFilesMap.entrySet()) {
			if (sourceFilesMap.get(entry.getKey()) == null) {
				File file = entry.getValue();
				missingInSource.add(DataMap.buildMap("path", FileUtil.getFilePath(file), "key", entry.getKey(), "name",
						file.getName(), "type", file.isFile() ? "file" : "directory", "size", file.length(),
						"lastModified", DateFormatUtils.format(new Date(file.lastModified()), "MMM dd yy, HH:mm:SS")));
			}
		}

		dataMap.put("missingInTarget", missingInTarget);
		dataMap.put("missingInSource", missingInSource);
		dataMap.put("conflictingFiles", conflictingFiles);
		return dataMap;
	}

	/**
	 * @param aFileCopyParams
	 * @return
	 */
	@Auditable
	@SuppressWarnings({ "static-method", "unchecked" })
	public DataMap copyFiles(DataMap aFileCopyParams) {
		File sourceRoot = new File(aFileCopyParams.getString("sourceRoot"));
		File targetRoot = new File(aFileCopyParams.getString("targetRoot"));
		boolean overwrite = aFileCopyParams.is("overwrite");
		StringBuilder errorMessage = new StringBuilder();
		int copyCount = 0;

		for (String key : (List<String>) aFileCopyParams.get("keyList")) {
			File source = new File(sourceRoot, key);
			File destination = new File(targetRoot, key);

			if (!source.exists()) {
				errorMessage.append(String.format("Source file [%s] is missing", source)).append(SEMICOLON_CHAR);
				continue;
			}

			if (destination.exists() && !overwrite) {
				errorMessage.append(String.format("Destination file [%s] exists", destination)).append(SEMICOLON_CHAR);
				continue;
			}

			copyCount += FileUtil.copyViaOS(source, destination, overwrite) ? 1 : 0;

		}

		DataMap dataMap = new DataMap();
		dataMap.put("errorMessage", errorMessage);
		dataMap.put("copyCount", copyCount);
		return dataMap;
	}
}
