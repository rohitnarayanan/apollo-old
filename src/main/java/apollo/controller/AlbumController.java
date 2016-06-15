package apollo.controller;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import accelerate.web.AccelerateWebResponse;
import apollo.model.Mp3Tag;
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
	 * @param aParseTagTokens
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/parseTags")
	@HandleError
	public AccelerateWebResponse parseTags(@RequestParam(name = "albumPath") String aAlbumPath,
			@RequestParam(name = "parseTagTokens") String aParseTagTokens) {
		AccelerateWebResponse model = new AccelerateWebResponse();
		model.put("albumPath", aAlbumPath);
		model.putAll(this.tagService.parseTags(aAlbumPath, aParseTagTokens));
		return model;
	}

	/**
	 * @param aAlbumTag
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/albumTag")
	@HandleError
	public AccelerateWebResponse saveAlbumTag(@RequestBody Mp3Tag aAlbumTag) {
		this.tagService.saveCommonTag(aAlbumTag, aAlbumTag.getString("albumPath"));

		AccelerateWebResponse model = new AccelerateWebResponse();
		model.put("savedTag", aAlbumTag);
		return model;
	}

	/**
	 * @param aTrackTag
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/trackTag")
	@HandleError
	public AccelerateWebResponse saveTrackTag(@RequestBody Mp3Tag aTrackTag) {
		this.tagService.saveTag(aTrackTag, new File(aTrackTag.getString("trackPath")));

		AccelerateWebResponse model = new AccelerateWebResponse();
		model.put("savedTag", aTrackTag);
		return model;
	}
}
