package apollo.controller;

import java.nio.file.Paths;
import java.util.List;

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
import apollo.model.Mp3Tag;
import apollo.service.AlbumService;
import apollo.util.HandleError;

/**
 * Controller mapped for Album operations
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since December 11, 2017
 */
@RestController
@RequestMapping("/album")
public class AlbumController {
	/**
	 * {@link Logger} instance
	 */
	@AutowireLogger
	private Logger _logger = null;

	/**
	 * {@link AlbumService} instance
	 */
	@Autowired
	private AlbumService albumService = null;

	/**
	 * @param aAlbumPath
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/songs")
	@HandleError
	public Response albumSongs(@RequestParam(name = "albumPath") String aAlbumPath) {
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
		return albumSongs(aAlbumPath);
	}

	/**
	 * @param aAlbumTag
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/saveAlbumTag")
	@HandleError
	public Response saveAlbumTag(@RequestBody Mp3Tag aAlbumTag) {
		this.albumService.saveAlbumTag(aAlbumTag);
		Response model = new Response();
		model.put("saveFlag", true);
		return model;
	}

	/**
	 * @param aSongTags
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/saveSongTags")
	@HandleError
	public Response saveTrackTags(@RequestBody List<Mp3Tag> aSongTags) {
		this.albumService.saveAlbumTags(aSongTags);
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
		DataMap attributes = this.albumService.renameAlbumSongs(aAlbumTag);
		Response model = albumSongs(attributes.get("albumPath").toString());
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
		Response model = albumSongs(attributes.get("albumPath").toString());
		model.putAll(attributes);
		return model;
	}
}
