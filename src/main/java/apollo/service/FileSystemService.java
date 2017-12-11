package apollo.service;

import static accelerate.utils.CommonConstants.COMMA_CHAR;
import static accelerate.utils.CommonConstants.HYPHEN_CHAR;
import static accelerate.utils.CommonConstants.SEMICOLON_CHAR;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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

import accelerate.utils.CommonUtils;
import accelerate.utils.NIOUtil;
import accelerate.utils.bean.DataMap;
import accelerate.utils.logging.Log;
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
	@Log
	public DataMap getFileTree(String aDirPath, String aDirName, final String aFileType) {
		String dirPath = StringUtils.isEmpty(aDirPath) ? this.apolloConfigProps.getFileSelectorRoot() : aDirPath;
		Path targetPath = StringUtils.isEmpty(aDirName) ? Paths.get(dirPath) : Paths.get(dirPath).resolve(aDirName);

		LOGGER.debug("Fetching file tree for [{}]", targetPath);

		DataMap dataMap = new DataMap();
		dataMap.put("path", targetPath);

		if (!targetPath.isDirectory()) {
			dataMap.put("message", "Root not a valid directory");
			return dataMap;
		}

		List<DataMap> fileTree = Arrays.stream(targetPath.listFiles(new FileFilter() {
			@Override
			public boolean accept(File aFile) {
				if (aFile.getName().startsWith(".")) {
					return false;
				}

				if (CommonUtils.compare(aFileType, "all")) {
					return true;
				} else if (CommonUtils.compare(aFileType, "none")) {
					return aFile.isDirectory();
				}

				return aFile.isDirectory() || CommonUtils.compare(aFileType, getFileExtn(aFile));
			}
		})).map(aFile -> {
			boolean children = false;
			if (aFile.isDirectory()) {
				if (CommonUtils.compare(aFileType, "all")) {
					children = !ObjectUtils.isEmpty(aFile.listFiles());
				} else {
					children = !ObjectUtils.isEmpty(aFile.listFiles(new FileFilter() {
						@Override
						public boolean accept(File aInnerFile) {
							return aInnerFile.isDirectory() || (CommonUtils.compare(aFileType, "none") ? false
									: CommonUtils.compare(aFileType, getFileExtn(aInnerFile)));
						}
					}));
				}
			}

			return DataMap.buildMap("text", aFile.getName(), "data",
					DataMap.buildMap("path", getFilePath(targetDir), "type",
							(aFile.isDirectory()) ? "folder" : getFileExtn(aFile)),
					"children", children, "icon", (aFile.isDirectory()) ? null : "fa fa-file-audio-o");
		}).collect(Collectors.toList());

		dataMap.put("fileTree", fileTree);
		return dataMap;
	}

	/**
	 * @param aInputParams
	 * @return
	 */
	@Log
	@SuppressWarnings("static-method")
	public DataMap compareFolders(Map<String, String> aInputParams) {
		/*
		 * Validate Input
		 */
		DataMap dataMap = new DataMap();
		StringBuilder message = new StringBuilder();
		Path sourceFolder = Paths.get(aInputParams.getOrDefault("sourcePath", "/INVALID_FOLDER_ROOT"));
		Path targetFolder = Paths.get(aInputParams.getOrDefault("targetPath", "/INVALID_FOLDER_ROOT"));

		if (!Files.exists(sourceFolder) || !Files.isDirectory(sourceFolder)) {
			message.append("Source is not a valid directory !!");
			message.append(HYPHEN_CHAR);
			message.append(aInputParams.get("sourcePath"));
			dataMap.put("errorFlag", true);
		}

		if (!Files.exists(targetFolder) || !Files.isDirectory(targetFolder)) {
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

		// traverse source folder
		Map<String, Path> sourceFilesMap = NIOUtil.walkFileTree(sourceFolder,
				(aFolder -> Files.exists(targetFolder.resolve(sourceFolder.relativize(aFolder)))
						? FileVisitResult.CONTINUE
						: FileVisitResult.SKIP_SUBTREE),
				null, null,
				(aFile, aFileVisitResult) -> !CommonUtils.compareAny(NIOUtil.getFileExtn(aFile), ignoreExtensions));

		// traverse target folder
		Map<String, Path> targetFilesMap = NIOUtil.walkFileTree(targetFolder,
				(aFolder -> Files.exists(sourceFolder.resolve(targetFolder.relativize(aFolder)))
						? FileVisitResult.CONTINUE
						: FileVisitResult.SKIP_SUBTREE),
				null, null,
				(aFile, aFileVisitResult) -> !CommonUtils.compareAny(NIOUtil.getFileExtn(aFile), ignoreExtensions));

		List<DataMap> missingInTarget = new ArrayList<>();
		List<DataMap> missingInSource = new ArrayList<>();
		List<DataMap> conflictingFiles = new ArrayList<>();

		for (Entry<String, Path> entry : sourceFilesMap.entrySet()) {
			Path sourceFile = entry.getValue();
			Path targetFile = targetFilesMap.get(entry.getKey());
			if (targetFile == null) {
				missingInTarget.add(DataMap.buildMap("path", sourceFile, "key", entry.getKey(), "name",
						NIOUtil.getFileName(sourceFile), "type", Files.isDirectory(sourceFile) ? "directory" : "file",
						"size", Files.size(sourceFile), "lastModified", DateFormatUtils
								.format(Files.getLastModifiedTime(sourceFile).toMillis(), "MMM dd yy, HH:mm:SS")));

			} else if (Files.size(sourceFile) == Files.size(targetFile)) {
				continue;
			} else if (!Files.isDirectory(sourceFile)) {
				// conflicted files
				conflictingFiles.add(DataMap.buildMap("key", entry.getKey(), "type",
						Files.isDirectory(sourceFile) ? "directory" : "file", "source",
						DataMap.buildMap("path", sourceFile, "name", NIOUtil.getFileName(sourceFile), "size",
								Files.size(sourceFile), "lastModified",
								DateFormatUtils.format(
										Files.getLastModifiedTime(sourceFile).toMillis(), "MMM dd yy, HH:mm:SS")),
						"target",
						DataMap.buildMap("path", targetFile, "name", NIOUtil.getFileName(targetFile), "size",
								Files.size(targetFile), "lastModified", DateFormatUtils.format(
										Files.getLastModifiedTime(targetFile).toMillis(), "MMM dd yy, HH:mm:SS"))));
			}
		}

		for (Entry<String, Path> entry : targetFilesMap.entrySet()) {
			if (sourceFilesMap.get(entry.getKey()) == null) {
				File file = entry.getValue();
				missingInSource.add(DataMap.buildMap("path", getFilePath(file), "key", entry.getKey(), "name",
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
	@Log
	@SuppressWarnings({ "static-method", "unchecked" })
	public DataMap copyFiles(DataMap aFileCopyParams) {
		Path sourceRoot = Paths.get(aFileCopyParams.getString("sourceRoot"));
		Path targetRoot = new File(aFileCopyParams.getString("targetRoot"));
		boolean overwrite = aFileCopyParams.is("overwrite");
		StringBuilder errorMessage = new StringBuilder();
		int copyCount = 0;

		for (String key : (List<String>) aFileCopyParams.get("keyList")) {
			Path source = new File(sourceRoot, key);
			Path destination = new File(targetRoot, key);

			if (!source.exists()) {
				errorMessage.append(String.format("Source file [%s] is missing", source)).append(SEMICOLON_CHAR);
				continue;
			}

			if (destination.exists() && !overwrite) {
				errorMessage.append(String.format("Destination file [%s] exists", destination)).append(SEMICOLON_CHAR);
				continue;
			}

			copyCount += Files.copy(source, destination, overwrite) ? 1 : 0;

		}

		DataMap dataMap = new DataMap();
		dataMap.put("errorMessage", errorMessage);
		dataMap.put("copyCount", copyCount);
		return dataMap;
	}
}
