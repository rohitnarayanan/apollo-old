package apollo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import accelerate.databean.DataMap;
import accelerate.web.AccelerateWebResponse;
import apollo.model.Mp3Tag;
import apollo.service.SongService;
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
	private SongService songService = null;

	/**
	 * @param aSongPath
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/getTag")
	@HandleError
	public AccelerateWebResponse getTag(@RequestParam(name = "songPath") String aSongPath) {
		AccelerateWebResponse model = new AccelerateWebResponse();
		model.putAll(this.songService.getTag(aSongPath));
		return model;
	}

	/**
	 * @param aSongPath
	 * @param aParseTagTokens
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/parseTags")
	@HandleError
	public AccelerateWebResponse parseTags(@RequestParam(name = "songPath") String aSongPath,
			@RequestParam(name = "parseTagTokens") String aParseTagTokens) {
		AccelerateWebResponse model = new AccelerateWebResponse();
		model.put("parsedTag", this.songService.parseSongTags(aSongPath, aParseTagTokens, false));
		return model;
	}

	/**
	 * @param aSongPath
	 * @param aParseTagTokens
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/saveParsedTags")
	@HandleError
	public AccelerateWebResponse saveParsedTags(@RequestParam(name = "songPath") String aSongPath,
			@RequestParam(name = "parseTagTokens") String aParseTagTokens) {
		this.songService.parseSongTags(aSongPath, aParseTagTokens, true);
		return getTag(aSongPath);
	}

	/**
	 * @param aSongTag
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/saveTag")
	@HandleError
	public void saveSongTag(@RequestBody Mp3Tag aSongTag) {
		this.songService.saveSongTag(aSongTag);
	}

	/**
	 * @param aSongPath
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/addToLibrary")
	@HandleError
	public AccelerateWebResponse addToLibrary(@RequestParam(name = "songPath") String aSongPath) {
		DataMap attributes = this.songService.addToLibrary(aSongPath);
		AccelerateWebResponse model = getTag(attributes.getString("songPath"));
		model.putAll(attributes);
		return model;
	}
}
