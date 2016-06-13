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
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/folders")
	@HandleError
	public AccelerateWebResponse listFolders(@RequestParam(name = "dirPath", defaultValue = "") String aDirPath,
			@RequestParam(name = "dirName", defaultValue = "") String aDirName) {
		AccelerateWebResponse model = new AccelerateWebResponse();
		model.putAll(this.fileSystemService.listFolders(aDirPath, aDirName));
		return model;
	}
}
