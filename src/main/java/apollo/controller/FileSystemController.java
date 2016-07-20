package apollo.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import accelerate.databean.DataMap;
import accelerate.web.AccelerateWebResponse;
import apollo.service.FileSystemService;
import apollo.util.HandleError;

/**
 * PUT DESCRIPTION HERE
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since Apr 15, 2016
 */
@RestController
@RequestMapping("/fileSystem")
public class FileSystemController {
	/**
	 * 
	 */
	@Autowired
	private FileSystemService fileSystemService = null;

	/**
	 * @param aDirPath
	 * @param aDirName
	 * @param aFileType
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/fileTree")
	@HandleError
	public AccelerateWebResponse getFileTree(@RequestParam(name = "dirPath", defaultValue = "") String aDirPath,
			@RequestParam(name = "dirName", defaultValue = "") String aDirName,
			@RequestParam(name = "fileType", defaultValue = "") String aFileType) {
		AccelerateWebResponse model = new AccelerateWebResponse();
		model.putAll(this.fileSystemService.getFileTree(aDirPath, aDirName, aFileType));
		return model;
	}

	/**
	 * @param aRequestParams
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/compareFolders")
	@HandleError
	public AccelerateWebResponse compareFolders(@RequestParam Map<String, String> aRequestParams) {
		AccelerateWebResponse model = new AccelerateWebResponse();
		model.putAll(this.fileSystemService.compareFolders(aRequestParams));
		return model;
	}

	/**
	 * @param aFileCopyParams
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/copyFile")
	@HandleError
	public AccelerateWebResponse compareFolders(@RequestBody DataMap aFileCopyParams) {
		AccelerateWebResponse model = new AccelerateWebResponse();
		model.putAll(this.fileSystemService.copyFiles(aFileCopyParams));
		return model;
	}
}
