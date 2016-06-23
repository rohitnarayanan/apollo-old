package apollo.util;

import static accelerate.util.AppUtil.compare;
import static accelerate.util.FileUtil.getParentName;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagException;

import accelerate.exception.AccelerateException;
import accelerate.util.FileUtil;
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
	 * @param aTrack
	 * @return
	 */
	public static MP3File getMP3File(File aTrack) {
		try {
			return new MP3File(aTrack);
		} catch (IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException error) {
			throw new AccelerateException(error);
		}
	}

	/**
	 * @param aTracks
	 * @return
	 * @throws AccelerateException
	 */
	public static Mp3Tag readCommonTag2(List<File> aTracks) throws AccelerateException {
		Mp3Tag commonTag = new Mp3Tag();
		aTracks.stream().parallel().forEach(aTrack -> {
			Mp3Tag mp3Tag = new Mp3Tag(aTrack);
			commonTag.extractCommonTag(mp3Tag);
		});

		return commonTag;
	}

	/**
	 * Function to validate the track. It checks if the file name and location
	 * match the tag values
	 * 
	 * @param aTrack
	 * @return {@link Set} containing all errors in the track
	 */
	public static Set<String> validateTrack(File aTrack) {
		int parentIndex = 1;
		Mp3Tag mp3Tag = new Mp3Tag(aTrack);
		Set<String> errorSet = new HashSet<>();

		String tagTitle = mp3Tag.getTitle();
		String fileTitle = FileUtil.getFileName(aTrack);
		if (!compare(tagTitle, fileTitle)) {
			errorSet.add("Mismatch Title:" + tagTitle + "|" + fileTitle);
		}

		String tagAlbum = mp3Tag.getAlbum();
		String fileAlbum = getParentName(aTrack, parentIndex++);
		if (!compare(tagAlbum, fileAlbum)) {
			String fileAlbumWithVolumeName = getParentName(aTrack, parentIndex++) + " " + fileAlbum;
			parentIndex++;

			if (!compare(tagAlbum, fileAlbumWithVolumeName)) {
				if (!compare(tagAlbum + " Album", fileAlbumWithVolumeName)) {
					errorSet.add("Mismatch Album:" + tagAlbum + "|" + fileAlbum + "|" + fileAlbumWithVolumeName);
				}
			}
		}

		String tagAlbumArtist = mp3Tag.getAlbumArtist();
		String fileAlbumArtist = getParentName(aTrack, parentIndex++);
		if (!compare(tagAlbumArtist, fileAlbumArtist)) {
			errorSet.add("Mismatch AlbumArtist:" + tagAlbumArtist + "|" + fileAlbumArtist);
		}

		String tagGenre = mp3Tag.getGenre();
		String fileGenre = getParentName(aTrack, parentIndex++);
		if (!compare(tagGenre, fileGenre)) {
			errorSet.add("Mismatch Genre:" + tagGenre + "|" + fileGenre);
		}

		String tagLanguage = mp3Tag.getLanguage();
		String fileLanguage = getParentName(aTrack, parentIndex++);
		if (!compare(tagLanguage, fileLanguage)) {
			errorSet.add("Mismatch Language:" + tagLanguage + "|" + fileLanguage);
		}

		return errorSet;
	}

	/**
	 * @param aPattern
	 * @return parsed tokens
	 */
	public static List<String> parseTagExpression(String aPattern) {
		return Arrays.stream(StringUtils.split(aPattern, "<")).filter(token -> StringUtils.isNotBlank(token))
				.flatMap(token -> {
					int index = token.indexOf(">");
					if (index < 0) {
						return Stream.of(token);
					}

					return Stream.of(token.substring(0, index + 1), token.substring(index + 1));
				}).collect(Collectors.toList());
	}
}