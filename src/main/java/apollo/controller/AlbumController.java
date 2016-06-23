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
import apollo.service.AlbumService;
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
	private AlbumService albumService = null;

	/**
	 * 
	 */
	@Autowired
	private TagService tagService = null;

	/**
	 * @param aAlbumPath
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/listTracks")
	@HandleError
	public AccelerateWebResponse listTracks(@RequestParam(name = "albumPath") String aAlbumPath) {
		AccelerateWebResponse model = new AccelerateWebResponse();
		model.put("albumPath", aAlbumPath);
		model.putAll(this.albumService.getAlbumTags(aAlbumPath));
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
		model.putAll(this.tagService.parseTags(aAlbumPath, aParseTagTokens, false));
		return model;
	}

	/**
	 * @param aAlbumPath
	 * @param aParseTagTokens
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/saveParsedTags")
	@HandleError
	public AccelerateWebResponse saveParsedTags(@RequestParam(name = "albumPath") String aAlbumPath,
			@RequestParam(name = "parseTagTokens") String aParseTagTokens) {
		this.tagService.parseTags(aAlbumPath, aParseTagTokens, true);
		return listTracks(aAlbumPath);
	}

	/**
	 * @param aAlbumTag
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/saveAlbumTag")
	@HandleError
	public AccelerateWebResponse saveAlbumTag(@RequestBody Mp3Tag aAlbumTag) {
		this.tagService.saveCommonTag(aAlbumTag, aAlbumTag.getFilePath());

		AccelerateWebResponse model = new AccelerateWebResponse();
		model.put("savedTag", aAlbumTag);
		return model;
	}

	/**
	 * @param aTrackTag
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/saveTrackTag")
	@HandleError
	public AccelerateWebResponse saveTrackTag(@RequestBody Mp3Tag aTrackTag) {
		this.tagService.saveTag(aTrackTag, new File(aTrackTag.getFilePath()));

		AccelerateWebResponse model = new AccelerateWebResponse();
		model.put("savedTag", aTrackTag);
		return model;
	}
}
