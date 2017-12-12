package apollo.controller;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import accelerate.utils.bean.DataMap;
import accelerate.utils.logging.AutowireLogger;
import accelerate.web.Response;
import apollo.service.FileSystemService;
import apollo.util.HandleError;

/**
 * Controller mapped for FileSystem operations
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since December 11, 2017
 */
@RestController
@RequestMapping("/fileSystem")
public class FileSystemController {
	/**
	 * {@link Logger} instance
	 */
	@AutowireLogger
	private Logger _logger = null;

	/**
	 * {@link FileSystemService} instance
	 */
	@Autowired
	private FileSystemService fileSystemService = null;

	/**
	 * @param aDirPath
	 * @param aDirName
	 * @param aFileType
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/fileTree")
	@HandleError
	public Response getFileTree(@RequestParam(name = "dirPath", defaultValue = "") String aDirPath,
			@RequestParam(name = "dirName", defaultValue = "") String aDirName,
			@RequestParam(name = "fileType", defaultValue = "") String aFileType) throws IOException {
		Response model = new Response();
		model.putAll(this.fileSystemService.getFileTree(aDirPath, aDirName, aFileType));
		return model;
	}

	/**
	 * @param aRequestParams
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/compareFolders")
	@HandleError
	public Response compareFolders(@RequestParam Map<String, String> aRequestParams) throws IOException {
		Response model = new Response();
		model.putAll(this.fileSystemService.compareFolders(aRequestParams));
		return model;
	}

	/**
	 * @param aFileCopyParams
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/copyFile")
	@HandleError
	public Response compareFolders(@RequestBody DataMap aFileCopyParams) throws IOException {
		Response model = new Response();
		model.putAll(this.fileSystemService.copyFiles(aFileCopyParams));
		return model;
	}
}
