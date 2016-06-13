package apollo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import accelerate.web.AccelerateWebResponse;
import apollo.service.TagService;
import apollo.util.HandleError;

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
	@Autowired
	private TagService tagService = null;

	/**
	 * @param aAlbumPath
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/tracks")
	@HandleError
	public AccelerateWebResponse albumTracks(@RequestParam(name = "albumPath") String aAlbumPath) {
		AccelerateWebResponse model = new AccelerateWebResponse();
		model.put("albumPath", aAlbumPath);
		model.putAll(this.tagService.readTags(aAlbumPath));
		return model;
	}

	/**
	 * @param aAlbumPath
	 * @param aTagExpressions
	 * @return
	 */
	@SuppressWarnings({ "static-method", "unused" })
	@RequestMapping(method = RequestMethod.GET, path = "/parseTags")
	@HandleError
	public AccelerateWebResponse parseTags(@RequestParam(name = "albumPath") String aAlbumPath,
			@RequestParam(name = "tagExpressions") String aTagExpressions) {
		AccelerateWebResponse model = new AccelerateWebResponse();
		return model;
	}
}
