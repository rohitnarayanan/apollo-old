package apollo.util;

import static accelerate.utils.CommonUtils.compare;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import accelerate.utils.NIOUtil;
import accelerate.utils.exception.AccelerateException;
import apollo.model.Mp3Tag;

/**
 * This class holds utility methods to manage ID3 tags for Mp3 files
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since January 13, 2011
 */
public class Mp3TagUtil {
	/**
	 * {@link Logger} instance
	 */
	private static final Logger _LOGGER = LoggerFactory.getLogger(Mp3TagUtil.class);

	/**
	 * hidden constructor
	 */
	private Mp3TagUtil() {
	}

	/**
	 * @param aMp3Path
	 * @return
	 */
	public static MP3File getMP3File(Path aMp3Path) {
		try {
			return new MP3File(aMp3Path.toFile());
		} catch (IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException error) {
			throw new AccelerateException(error);
		}
	}

	/**
	 * Function to validate the mp3 tag. It checks if the file name and location
	 * match the tag values
	 * 
	 * @param aMp3Path
	 * @return {@link Set} containing all errors in the tag
	 */
	public static Set<String> validateTag(Path aMp3Path) {
		int parentIndex = 1;
		Mp3Tag mp3Tag = new Mp3Tag(aMp3Path);
		Set<String> errorSet = new HashSet<>();

		String tagTitle = mp3Tag.getTitle();
		String fileTitle = NIOUtil.getBaseName(aMp3Path);
		if (!compare(tagTitle, fileTitle)) {
			errorSet.add("Mismatch Title:" + tagTitle + "|" + fileTitle);
		}

		String tagAlbum = mp3Tag.getAlbum();
		Path albumPath = NIOUtil.getParent(aMp3Path, parentIndex++);
		if (!compare(tagAlbum, albumPath.getFileName())) {
			String fileAlbumWithVolumeName = albumPath.getParent() + " " + albumPath.getFileName();

			if (!compare(tagAlbum, fileAlbumWithVolumeName)) {
				errorSet.add(
						"Mismatch Album:" + tagAlbum + "|" + albumPath.getFileName() + "|" + fileAlbumWithVolumeName);
			} else {
				parentIndex++;
			}
		}

		String tagAlbumArtist = mp3Tag.getAlbumArtist();
		Path albumArtistPath = NIOUtil.getParent(aMp3Path, parentIndex++);
		if (!compare(tagAlbumArtist, albumArtistPath.getFileName())) {
			errorSet.add("Mismatch AlbumArtist:" + tagAlbumArtist + "|" + albumArtistPath.getFileName());
		}

		String tagGenre = mp3Tag.getGenre();
		Path genrePath = NIOUtil.getParent(aMp3Path, parentIndex++);
		if (!compare(tagGenre, genrePath.getFileName())) {
			errorSet.add("Mismatch Genre:" + tagGenre + "|" + genrePath.getFileName());
		}

		_LOGGER.trace("validateTag: [{}] [{}]", aMp3Path, errorSet);
		return errorSet;
	}

	/**
	 * @param aPattern
	 * @return parsed tokens
	 */
	public static List<String> parseTagExpression(String aPattern) {
		List<String> tagExpressions = Arrays.stream(StringUtils.split(aPattern, "<"))
				.filter(token -> StringUtils.isNotBlank(token)).flatMap(token -> {
					int index = token.indexOf(">");
					if (index < 0) {
						return Stream.of(token);
					}

					return Stream.of(token.substring(0, index + 1), token.substring(index + 1));
				}).collect(Collectors.toList());

		_LOGGER.trace("parseTagExpression: [{}] [{}]", aPattern, tagExpressions);
		return tagExpressions;
	}

	/**
	 * This method deletes the given tag from the Mp3 file
	 *
	 * @param aMp3Path
	 * @return Mp3Tag instance
	 * @throws AccelerateException
	 */
	public static Mp3Tag deleteV1Tag(Path aMp3Path) throws AccelerateException {
		try {
			MP3File mp3File = getMP3File(aMp3Path);
			Tag tag = mp3File.getID3v1Tag();

			if (tag != null) {
				_LOGGER.trace("deleteV1Tag: deleting v1 tag from [{}]", aMp3Path);
				mp3File.delete(mp3File.getID3v1Tag());
				mp3File.commit();
				mp3File.save();
			}
		} catch (IOException | TagException | CannotWriteException error) {
			throw new AccelerateException(error);
		}

		return new Mp3Tag(aMp3Path);
	}
}