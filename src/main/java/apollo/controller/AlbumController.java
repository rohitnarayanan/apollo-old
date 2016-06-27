package apollo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import accelerate.databean.DataMap;
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
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/saveAlbumTag")
	@HandleError
	public void saveAlbumTag(@RequestBody Mp3Tag aAlbumTag) {
		this.tagService.saveCommonTag(aAlbumTag, aAlbumTag.getFilePath());
	}

	/**
	 * @param aTrackTag
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/saveTrackTag")
	@HandleError
	public void saveTrackTag(@RequestBody Mp3Tag aTrackTag) {
		this.tagService.saveTag(aTrackTag);
	}

	/**
	 * @param aTrackTags
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/saveTrackTags")
	@HandleError
	public void saveTrackTags(@RequestBody List<Mp3Tag> aTrackTags) {
		this.tagService.saveTags(aTrackTags);
	}

	/**
	 * @param aAlbumTag
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/addToLibrary")
	@HandleError
	public AccelerateWebResponse addToLibrary(@RequestBody Mp3Tag aAlbumTag) {
		DataMap attributes = this.albumService.addToLibrary(aAlbumTag);
		AccelerateWebResponse model = listTracks(attributes.get("albumPath").toString());
		model.putAll(attributes);
		return model;
	}

}
