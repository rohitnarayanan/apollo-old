package apollo.service;

import static accelerate.utils.CommonConstants.COMMA_CHAR;
import static accelerate.utils.CommonConstants.HYPHEN_CHAR;
import static accelerate.utils.CommonConstants.SEMICOLON_CHAR;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
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

import accelerate.utils.CommonUtils;
import accelerate.utils.NIOUtil;
import accelerate.utils.bean.DataMap;
import accelerate.utils.exception.AccelerateException;
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
	 * @throws IOException
	 */
	@Log
	public DataMap getFileTree(String aDirPath, String aDirName, final String aFileType) throws IOException {
		Path dirPath = StringUtils.isEmpty(aDirPath) ? Paths.get(this.apolloConfigProps.getFileSelectorRoot())
				: Paths.get(aDirPath);
		Path targetPath = StringUtils.isEmpty(aDirName) ? dirPath : dirPath.resolve(aDirName);

		LOGGER.debug("Fetching file tree for [{}]", targetPath);

		DataMap dataMap = new DataMap();
		dataMap.put("path", targetPath);

		if (!Files.isDirectory(targetPath)) {
			dataMap.put("message", "Root not a valid directory");
			return dataMap;
		}

		List<DataMap> fileTree = Files.list(targetPath).filter(aPath -> {
			if (aPath.getFileName().startsWith(".")) {
				return false;
			}

			if (CommonUtils.compare(aFileType, "all")) {
				return true;
			} else if (CommonUtils.compare(aFileType, "none")) {
				return Files.isDirectory(aPath);
			}

			return Files.isDirectory(aPath) || CommonUtils.compare(aFileType, NIOUtil.getFileExtn(aPath));
		}).parallel().map(aPath -> {
			boolean children = false;
			if (Files.isDirectory(aPath)) {
				try {
					if (CommonUtils.compare(aFileType, "all")) {
						children = Files.list(aPath).count() > 0;
					} else {
						children = Files.list(aPath).parallel().filter(aInnerPath -> {
							return Files.isDirectory(aInnerPath) || (CommonUtils.compare(aFileType, "none") ? false
									: CommonUtils.compare(aFileType, NIOUtil.getFileExtn(aInnerPath)));
						}).count() > 0;
					}
				} catch (IOException error) {
					throw new AccelerateException(error);
				}
			}

			return DataMap.buildMap("text", aPath.getFileName(), "data",
					DataMap.buildMap("path", NIOUtil.getPathString(aPath), "type",
							Files.isDirectory(aPath) ? "folder" : NIOUtil.getFileExtn(aPath)),
					"children", children, "icon", Files.isDirectory(aPath) ? null : "fa fa-file-audio-o");
		}).collect(Collectors.toList());

		dataMap.put("fileTree", fileTree);
		return dataMap;
	}

	/**
	 * @param aInputParams
	 * @return
	 * @throws IOException
	 */
	@Log
	@SuppressWarnings("static-method")
	public DataMap compareFolders(Map<String, String> aInputParams) throws IOException {
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
				(aDirPath -> Files.exists(targetFolder.resolve(NIOUtil.getRelativePath(sourceFolder, aDirPath)))), null,
				null, null, null,
				(aFile, aFileVisitResult) -> !CommonUtils.compareAny(NIOUtil.getFileExtn(aFile), ignoreExtensions));

		// traverse target folder
		Map<String, Path> targetFilesMap = NIOUtil.walkFileTree(targetFolder,
				(aDirPath -> Files.exists(sourceFolder.resolve(NIOUtil.getRelativePath(targetFolder, aDirPath)))), null,
				null, null, null,
				(aFile, aFileVisitResult) -> !CommonUtils.compareAny(NIOUtil.getFileExtn(aFile), ignoreExtensions));

		List<DataMap> missingInTarget = new ArrayList<>();
		List<DataMap> missingInSource = new ArrayList<>();
		List<DataMap> conflictingFiles = new ArrayList<>();

		// check source files
		for (Entry<String, Path> entry : sourceFilesMap.entrySet()) {
			Path sourcePath = entry.getValue();
			Path targetPath = targetFilesMap.get(entry.getKey());
			if (targetPath == null) {
				missingInTarget.add(DataMap.buildMap("path", sourcePath, "key", entry.getKey(), "name",
						sourcePath.getFileName(), "type", Files.isDirectory(sourcePath) ? "directory" : "file", "size",
						Files.size(sourcePath), "lastModified", DateFormatUtils
								.format(Files.getLastModifiedTime(sourcePath).toMillis(), "MMM dd yy, HH:mm:SS")));

			} else if (Files.size(sourcePath) == Files.size(targetPath)) {
				continue;
			} else if (!Files.isDirectory(sourcePath)) {
				// conflicted files
				conflictingFiles.add(DataMap.buildMap("key", entry.getKey(), "type",
						Files.isDirectory(sourcePath) ? "directory" : "file", "source",
						DataMap.buildMap("path", sourcePath, "name", sourcePath.getFileName(), "size",
								Files.size(sourcePath), "lastModified",
								DateFormatUtils.format(
										Files.getLastModifiedTime(sourcePath).toMillis(), "MMM dd yy, HH:mm:SS")),
						"target",
						DataMap.buildMap("path", targetPath, "name", targetPath.getFileName(), "size",
								Files.size(targetPath), "lastModified", DateFormatUtils.format(
										Files.getLastModifiedTime(targetPath).toMillis(), "MMM dd yy, HH:mm:SS"))));
			}
		}

		// check target files
		for (Entry<String, Path> entry : targetFilesMap.entrySet()) {
			if (sourceFilesMap.get(entry.getKey()) == null) {
				Path targetPath = entry.getValue();
				missingInSource.add(DataMap.buildMap("path", targetPath, "key", entry.getKey(), "name",
						targetPath.getFileName(), "type", Files.isDirectory(targetPath) ? "directory" : "file", "size",
						Files.size(targetPath), "lastModified", DateFormatUtils
								.format(Files.getLastModifiedTime(targetPath).toMillis(), "MMM dd yy, HH:mm:SS")));
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
	 * @throws IOException
	 */
	@Log
	@SuppressWarnings({ "static-method", "unchecked" })
	public DataMap copyFiles(DataMap aFileCopyParams) throws IOException {
		Path sourceRoot = Paths.get(aFileCopyParams.getString("sourceRoot"));
		Path targetRoot = Paths.get(aFileCopyParams.getString("targetRoot"));
		boolean overwrite = aFileCopyParams.is("overwrite");
		StringBuilder errorMessage = new StringBuilder();
		int copyCount = 0;

		for (String key : (List<String>) aFileCopyParams.get("keyList")) {
			Path source = sourceRoot.resolve(key);
			Path destination = targetRoot.resolve(key);

			if (!Files.exists(source)) {
				errorMessage.append(String.format("Source file [%s] is missing", source)).append(SEMICOLON_CHAR);
				continue;
			}

			if (Files.exists(destination) && !overwrite) {
				errorMessage.append(String.format("Destination file [%s] exists", destination)).append(SEMICOLON_CHAR);
				continue;
			}

			copyCount += Files.exists(Files.copy(source, destination, StandardCopyOption.ATOMIC_MOVE)) ? 1 : 0;
		}

		DataMap dataMap = new DataMap();
		dataMap.put("errorMessage", errorMessage);
		dataMap.put("copyCount", copyCount);
		return dataMap;
	}
}
