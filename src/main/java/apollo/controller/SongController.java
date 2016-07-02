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
@RequestMapping("/song")
public class SongController {
	/**
	 * 
	 */
	@Autowired
	private TagService tagService = null;

	/**
	 * @param aTrackPath
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/getTag")
	@HandleError
	public AccelerateWebResponse getTag(@RequestParam(name = "trackPath") String aTrackPath) {
		AccelerateWebResponse model = new AccelerateWebResponse();
		model.putAll(this.tagService.readTag(aTrackPath));
		return model;
	}
}
