package apollo.controller;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import accelerate.databean.AccelerateDataBean;
import accelerate.databean.AccelerateWebResponse;
import accelerate.util.AppUtil;
import accelerate.util.FileUtil;
import apollo.util.ApolloConstants;
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
	 * @param aDirPath
	 * @param aDirName
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/listFolders")
	public static AccelerateDataBean listFolders(@RequestParam(name = "dirPath", defaultValue = "") String aDirPath,
			@RequestParam(name = "dirName", defaultValue = "") String aDirName) {
		String path = AppUtil.isEmpty(aDirPath) ? "/Users" : aDirPath;
		String name = AppUtil.isEmpty(aDirName) ? "rohitnarayanan" : aDirName;
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
			dataBean.put("childFolders", !AppUtil.isEmpty(childFolders));
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
	public static AccelerateWebResponse listTracks(@RequestParam(name = "dirPath", defaultValue = "") String aDirPath,
			@RequestParam(name = "dirName", defaultValue = "") String aDirName) {
		String path = AppUtil.isEmpty(aDirPath) ? "~" : aDirPath;
		String name = AppUtil.isEmpty(aDirName) ? "" : aDirName;
		File targetDir = new File(path, name);

		AccelerateWebResponse response = new AccelerateWebResponse();
		response.put("tracks", Arrays.stream(targetDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File aFile) {
				return AppUtil.compare(FileUtil.getFileExtn(aFile), ApolloConstants.MP3);
			}
		})).map(aFile -> {
			try {
				return ID3Util.readTag(aFile);
			} catch (Exception error) {
				error.printStackTrace();
				return null;
			}
		}).collect(Collectors.toList()));

		return response;
	}
}
