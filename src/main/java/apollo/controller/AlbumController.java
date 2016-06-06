package apollo.controller;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping("/album")
public class AlbumController {
	/**
	 * 
	 */
	private static Logger _logger = LoggerFactory.getLogger(AlbumController.class);

	/**
	 * @param aDirPath
	 * @param aDirName
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/listFolders")
	@HandleError
	public static AccelerateDataBean listFolders(@RequestParam(name = "dirPath", defaultValue = "") String aDirPath,
			@RequestParam(name = "dirName", defaultValue = "") String aDirName) {
		String path = ObjectUtils.isEmpty(aDirPath) ? "/Users" : aDirPath;
		String name = ObjectUtils.isEmpty(aDirName) ? "rohitnarayanan" : aDirName;
		File targetDir = new File(path, name);

		AccelerateDataBean model = new AccelerateDataBean();
		model.put("dirPath", targetDir.getPath());
		model.put("folders", Arrays.stream(targetDir.listFiles(new FileFilter() {
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

		return model;
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

		AccelerateWebResponse response = new AccelerateWebResponse();
		response.put("tracks", Arrays.stream(targetDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File aFile) {
				return AppUtil.compare(FileUtil.getFileExtn(aFile), "txt");
			}
		})).map(aFile -> {
			try {
				return ID3Util.tempTag(aFile);
			} catch (Exception error) {
				_logger.error("Error [{}] in reading tags", error.getMessage(), error);
			}

			return null;
		}).collect(Collectors.toList()));

		return response;
	}
}
