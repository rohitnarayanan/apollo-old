package apollo.controller;

import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import accelerate.utils.bean.DataMap;
import accelerate.web.Response;
import apollo.model.Mp3Tag;
import apollo.service.AlbumService;
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
	 * @param aAlbumPath
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/listTracks")
	@HandleError
	public Response listTracks(@RequestParam(name = "albumPath") String aAlbumPath) {
		Response model = new Response();
		model.putAll(this.albumService.getAlbumTags(Paths.get(aAlbumPath)));
		return model;
	}

	/**
	 * @param aAlbumPath
	 * @param aParseTagTokens
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/parseTags")
	@HandleError
	public Response parseTags(@RequestParam(name = "albumPath") String aAlbumPath,
			@RequestParam(name = "parseTagTokens") String aParseTagTokens) {
		Response model = new Response();
		model.putAll(this.albumService.parseAlbumTags(Paths.get(aAlbumPath), aParseTagTokens, false));
		return model;
	}

	/**
	 * @param aAlbumPath
	 * @param aParseTagTokens
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/saveParsedTags")
	@HandleError
	public Response saveParsedTags(@RequestParam(name = "albumPath") String aAlbumPath,
			@RequestParam(name = "parseTagTokens") String aParseTagTokens) {
		this.albumService.parseAlbumTags(Paths.get(aAlbumPath), aParseTagTokens, true);
		return listTracks(aAlbumPath);
	}

	/**
	 * @param aAlbumTag
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/saveAlbumTag")
	@HandleError
	public Response saveAlbumTag(@RequestBody Mp3Tag aAlbumTag) {
		this.albumService.saveAlbumTag(aAlbumTag, aAlbumTag.getFilePath());
		Response model = new Response();
		model.put("saveFlag", true);
		return model;
	}

	/**
	 * @param aTrackTags
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/saveTrackTags")
	@HandleError
	public Response saveTrackTags(@RequestBody List<Mp3Tag> aTrackTags) {
		this.albumService.saveTrackTags(aTrackTags);
		Response model = new Response();
		model.put("saveFlag", true);
		return model;
	}

	/**
	 * @param aAlbumTag
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/renameTracks")
	@HandleError
	public Response renameTracks(@RequestBody Mp3Tag aAlbumTag) {
		DataMap attributes = this.albumService.renameTracks(aAlbumTag);
		Response model = listTracks(attributes.get("albumPath").toString());
		model.putAll(attributes);
		return model;
	}

	/**
	 * @param aAlbumTag
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/addToLibrary")
	@HandleError
	public Response addToLibrary(@RequestBody Mp3Tag aAlbumTag) {
		DataMap attributes = this.albumService.addToLibrary(aAlbumTag);
		Response model = listTracks(attributes.get("albumPath").toString());
		model.putAll(attributes);
		return model;
	}
}
