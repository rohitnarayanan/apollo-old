package apollo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
	 * @param aFoldersOnly
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/fileTree")
	@HandleError
	public AccelerateWebResponse getFileTree(@RequestParam(name = "dirPath", defaultValue = "") String aDirPath,
			@RequestParam(name = "dirName", defaultValue = "") String aDirName,
			@RequestParam(name = "foldersOnly", defaultValue = "") boolean aFoldersOnly) {
		AccelerateWebResponse model = new AccelerateWebResponse();
		model.putAll(this.fileSystemService.getFileTree(aDirPath, aDirName, aFoldersOnly));
		return model;
	}
}
