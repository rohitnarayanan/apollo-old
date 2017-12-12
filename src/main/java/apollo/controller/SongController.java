package apollo.controller;

import java.io.IOException;
import java.nio.file.Paths;

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
import apollo.service.SongService;
import apollo.util.HandleError;

/**
 * Controller mapped for Song operations
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since December 11, 2017
 */
@RestController
@RequestMapping("/track")
public class SongController {
	/**
	 * {@link Logger} instance
	 */
	@AutowireLogger
	private Logger _logger = null;

	/**
	 * {@link SongService} instance
	 */
	@Autowired
	private SongService songService = null;

	/**
	 * @param aTrackPath
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/getTag")
	@HandleError
	public Response getTag(@RequestParam(name = "trackPath") String aTrackPath) {
		Response model = new Response();
		model.putAll(this.songService.getTag(Paths.get(aTrackPath)));
		return model;
	}

	/**
	 * @param aTrackPath
	 * @param aParseTagTokens
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/parseTags")
	@HandleError
	public Response parseTags(@RequestParam(name = "trackPath") String aTrackPath,
			@RequestParam(name = "parseTagTokens") String aParseTagTokens) {
		Response model = new Response();
		model.put("parsedTag", this.songService.parseTags(Paths.get(aTrackPath), aParseTagTokens, false));
		return model;
	}

	/**
	 * @param aTrackPath
	 * @param aParseTagTokens
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/saveParsedTags")
	@HandleError
	public Response saveParsedTags(@RequestParam(name = "trackPath") String aTrackPath,
			@RequestParam(name = "parseTagTokens") String aParseTagTokens) {
		this.songService.parseTags(Paths.get(aTrackPath), aParseTagTokens, true);
		return getTag(aTrackPath);
	}

	/**
	 * @param aTrackTag
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/saveTag")
	@HandleError
	public Response saveTag(@RequestBody Mp3Tag aTrackTag) {
		this.songService.saveTag(aTrackTag);
		Response model = new Response();
		model.put("saveFlag", true);
		return model;
	}

	/**
	 * @param aTrackPath
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/addToLibrary")
	@HandleError
	public Response addToLibrary(@RequestParam(name = "trackPath") String aTrackPath) throws IOException {
		DataMap attributes = this.songService.addToLibrary(Paths.get(aTrackPath));
		Response model = getTag(attributes.getString("trackPath"));
		model.putAll(attributes);
		return model;
	}
}
