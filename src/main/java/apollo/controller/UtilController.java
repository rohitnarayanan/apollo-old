package apollo.controller;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import accelerate.cache.PropertyCache;
import accelerate.databean.AccelerateDataBean;
import accelerate.util.AppUtil;
import accelerate.util.FileUtil;
import accelerate.web.AccelerateWebResponse;
import apollo.util.HandleError;
import apollo.util.ID3Util;

/**
 * PUT DESCRIPTION HERE
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since Apr 15, 2016
 */
@RestController
@RequestMapping("/util")
public class UtilController {
	/**
	 * 
	 */
	private static Logger LOGGER = LoggerFactory.getLogger(UtilController.class);

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
	@RequestMapping(method = RequestMethod.GET, path = "/listFolders")
	@HandleError
	public AccelerateWebResponse listFolders(@RequestParam(name = "dirPath", defaultValue = "") String aDirPath,
			@RequestParam(name = "dirName", defaultValue = "") String aDirName) {
		String path = ObjectUtils.isEmpty(aDirPath) ? this.apolloProps.get("apollo.library.root") : aDirPath;
		String name = ObjectUtils.isEmpty(aDirName) ? this.apolloProps.get("apollo.library.name") : aDirName;
		File targetDir = new File(path, name);

		LOGGER.debug("Fetching sub folders for [{}]", targetDir);

		AccelerateWebResponse response = new AccelerateWebResponse();
		response.put("dirPath", targetDir.getPath());
		response.put("folders", Arrays.stream(targetDir.listFiles(new FileFilter() {
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

			AccelerateDataBean dataBean = new AccelerateDataBean();
			dataBean.put("name", aFile.getName());
			dataBean.put("childFolders", !ObjectUtils.isEmpty(childFolders));
			return dataBean;
		}).collect(Collectors.toList()));

		return response;
	}

	/**
	 * @param aDirPath
	 * @param aDirName
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/listTracks")
	@HandleError
	public static AccelerateWebResponse listTracks(@RequestParam(name = "dirPath", defaultValue = "") String aDirPath,
			@RequestParam(name = "dirName", defaultValue = "") String aDirName) {
		String path = ObjectUtils.isEmpty(aDirPath) ? "~" : aDirPath;
		String name = ObjectUtils.isEmpty(aDirName) ? "" : aDirName;
		File targetDir = new File(path, name);

		LOGGER.debug("Fetching tracks for [{}]", targetDir);

		AccelerateWebResponse response = new AccelerateWebResponse();
		response.put("dirPath", targetDir.getPath());
		response.put("tracks", Arrays.stream(targetDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File aFile) {
				return AppUtil.compare(FileUtil.getFileExtn(aFile), "zzz");
			}
		})).map(aFile -> {
			return ID3Util.tempTag(aFile);
		}).collect(Collectors.toList()));

		return response;
	}
}
