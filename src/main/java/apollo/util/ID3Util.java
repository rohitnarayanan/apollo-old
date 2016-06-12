package apollo.util;

import static accelerate.util.AccelerateConstants.EMPTY_STRING;
import static accelerate.util.AppUtil.compare;
import static accelerate.util.FileUtil.getFileExtn;
import static accelerate.util.FileUtil.getParentName;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.id3.AbstractTag;
import org.jaudiotagger.tag.id3.ID3v23Frames;
import org.jaudiotagger.tag.id3.ID3v24Frames;
import org.jaudiotagger.tag.id3.ID3v24Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import accelerate.exception.AccelerateException;
import accelerate.exception.FlowControlException;
import accelerate.util.AppUtil;
import accelerate.util.FileUtil;
import accelerate.util.ReflectionUtil;
import apollo.model.Mp3Tag;
import apollo.model.TagCheckResult;

/**
 * This class holds utility methods to manage ID3 tags for Mp3 files
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since January 13, 2011
 */
public class ID3Util {
	/**
	 * {@link Logger} instance
	 */
	protected static final Logger logger = LoggerFactory.getLogger(ID3Util.class);

	/**
	 * {@link Map} containing standard frame names
	 */
	private static Map<String, FieldKey> standardFrames = null;

	/**
	 * {@link Map} containing standard frame names
	 */
	private static List<FieldKey> standardFields = null;

	/**
	 * Static Initializer Block
	 */
	static {
		try {
			setupStandardFrames();
			setupStandardFields();
			// LogManager.getLogManager()
			// .readConfiguration(ID3Util.class.getResourceAsStream("/mp3Logger.properties"));
		} catch (Exception error) {
			logger.error(error.getMessage(), error);
		}
	}

	/**
	 * @param aTrack
	 * @return {@link MP3File} instance
	 * @throws AccelerateException
	 */
	public static MP3File getMP3File(File aTrack) throws AccelerateException {
		try {
			return new MP3File(aTrack);
		} catch (IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException error) {
			throw new AccelerateException(error);
		}
	}

	/**
	 * @param aTrack
	 * @return {@link Mp3Tag} instance
	 * @throws AccelerateException
	 */
	public static Mp3Tag readTag(File aTrack) throws AccelerateException {
		if (!compare(StringUtils.upperCase(getFileExtn(aTrack)), "MP3")) {
			throw new AccelerateException("Not a mp3: " + aTrack);
		}

		MP3File mp3File = getMP3File(aTrack);
		Tag tag = mp3File.getTag();

		Mp3Tag mp3Tag = new Mp3Tag();
		mp3Tag.sourceFile = aTrack;
		mp3Tag.fileName = aTrack.getName();
		mp3Tag.id = tag.getFirst(FieldKey.KEY);
		mp3Tag.language = tag.getFirst(FieldKey.LANGUAGE);
		mp3Tag.genre = tag.getFirst(FieldKey.GENRE);
		mp3Tag.mood = tag.getFirst(FieldKey.MOOD);
		mp3Tag.album = tag.getFirst(FieldKey.ALBUM);
		mp3Tag.year = tag.getFirst(FieldKey.YEAR);
		mp3Tag.albumArtist = tag.getFirst(FieldKey.ALBUM_ARTIST);
		mp3Tag.composer = tag.getFirst(FieldKey.COMPOSER);
		mp3Tag.artist = tag.getFirst(FieldKey.ARTIST);
		mp3Tag.trackNbr = tag.getFirst(FieldKey.TRACK);
		mp3Tag.title = tag.getFirst(FieldKey.TITLE);
		mp3Tag.lyrics = tag.getFirst(FieldKey.LYRICS);
		mp3Tag.tags = tag.getFirst(FieldKey.TAGS);

		Artwork artwork = tag.getFirstArtwork();
		if (artwork != null) {
			mp3Tag.artwork.encode(artwork);
		}

		AudioHeader audioHeader = mp3File.getAudioHeader();
		mp3Tag.header.size = mp3Tag.sourceFile.length();
		mp3Tag.header.length = audioHeader.getTrackLength();
		mp3Tag.header.mode = audioHeader.getChannels();
		mp3Tag.header.bitrateType = audioHeader.isVariableBitRate() ? "VBR" : "CBR";
		mp3Tag.header.bitrate = audioHeader.getBitRate();
		mp3Tag.header.frequency = audioHeader.getSampleRate();

		return mp3Tag;
	}

	/**
	 * This is the default method to write the tag to the given file. It calls
	 * the {@link #writeTag(File, Mp3Tag, int)} method with aWriteFlag argument
	 * as 0
	 *
	 * @param aTrack
	 *            Target file to which the ID3 tag is to be written
	 * @param mp3Tag
	 *            Tag information
	 * @throws AccelerateException
	 */
	public static void writeTag(File aTrack, Mp3Tag mp3Tag) throws AccelerateException {
		writeTag(aTrack, mp3Tag, 0);
	}

	/**
	 * This method writes the tag to the given file
	 *
	 * @param aTrack
	 *            Target file to which the ID3 tag is to be written
	 * @param aMp3Tag
	 *            Tag information
	 * @param aWriteFlag
	 *            <ul>
	 *            <li>0: Write only if tag is available else ignore</li>
	 *            <li>1: Write only if the tag is available else throw an
	 *            error</li>
	 *            <li>2: Write after creating a new tag, throw an error if tag
	 *            already present</li>
	 *            <li>3: Write after creating a new tag, if a tag is present
	 *            then delete it</li>
	 *            </ul>
	 * @throws AccelerateException
	 */
	public static void writeTag(File aTrack, Mp3Tag aMp3Tag, int aWriteFlag) throws AccelerateException {
		try {
			MP3File mp3File = getMP3File(aTrack);
			Tag tag = mp3File.getTag();

			if (aWriteFlag == 2) {
				if (tag != null) {
					mp3File.delete((AbstractTag) tag);
				}

				tag = new ID3v24Tag();
				mp3File.setTag(tag);
			}

			if (tag == null) {
				if (aWriteFlag == 1) {
					tag = new ID3v24Tag();
					mp3File.setTag(tag);
				} else {
					throw new AccelerateException("No Tag: " + aTrack);
				}
			}

			if (aMp3Tag.id != null) {
				if (aMp3Tag.id.length() == 0) {
					tag.deleteField(FieldKey.KEY);
				} else {
					tag.setField(FieldKey.KEY, aMp3Tag.id);
				}
			}

			if (aMp3Tag.language != null) {
				if (aMp3Tag.language.length() == 0) {
					tag.deleteField(FieldKey.LANGUAGE);
				} else {
					tag.setField(FieldKey.LANGUAGE, aMp3Tag.language);
				}
			}

			if (aMp3Tag.genre != null) {
				if (aMp3Tag.genre.length() == 0) {
					tag.deleteField(FieldKey.GENRE);
				} else {
					tag.setField(FieldKey.GENRE, aMp3Tag.genre);
				}
			}

			if (aMp3Tag.mood != null) {
				if (aMp3Tag.mood.length() == 0) {
					tag.deleteField(FieldKey.MOOD);
				} else {
					tag.setField(FieldKey.MOOD, aMp3Tag.mood);
				}
			}

			if (aMp3Tag.album != null) {
				if (aMp3Tag.album.length() == 0) {
					tag.deleteField(FieldKey.ALBUM);
				} else {
					tag.setField(FieldKey.ALBUM, aMp3Tag.album);
				}
			}

			if (aMp3Tag.year != null) {
				if (aMp3Tag.year.length() == 0) {
					tag.deleteField(FieldKey.YEAR);
				} else {
					tag.setField(FieldKey.YEAR, aMp3Tag.year);
				}
			}

			if (aMp3Tag.albumArtist != null) {
				if (aMp3Tag.albumArtist.length() == 0) {
					tag.deleteField(FieldKey.ALBUM_ARTIST);
				} else {
					tag.setField(FieldKey.ALBUM_ARTIST, aMp3Tag.albumArtist);
				}
			}

			if (aMp3Tag.composer != null) {
				if (aMp3Tag.composer.length() == 0) {
					tag.deleteField(FieldKey.COMPOSER);
				} else {
					tag.setField(FieldKey.COMPOSER, aMp3Tag.composer);
				}
			}

			if (aMp3Tag.artist != null) {
				if (aMp3Tag.artist.length() == 0) {
					tag.deleteField(FieldKey.ARTIST);
				} else {
					tag.setField(FieldKey.ARTIST, aMp3Tag.artist);
				}
			}

			if (aMp3Tag.trackNbr != null) {
				tag.deleteField(FieldKey.TRACK_TOTAL);
				if (aMp3Tag.trackNbr.length() == 0) {
					tag.deleteField(FieldKey.TRACK);
				} else {
					tag.setField(FieldKey.TRACK, aMp3Tag.trackNbr);
				}
			}

			if (aMp3Tag.title != null) {
				if (aMp3Tag.title.length() == 0) {
					tag.deleteField(FieldKey.TITLE);
				} else {
					tag.setField(FieldKey.TITLE, aMp3Tag.title);
				}
			}

			if (aMp3Tag.lyrics != null) {
				if (aMp3Tag.lyrics.length() == 0) {
					tag.deleteField(FieldKey.LYRICS);
				} else {
					tag.setField(FieldKey.LYRICS, aMp3Tag.lyrics);
				}
			}

			if (aMp3Tag.tags != null) {
				if (aMp3Tag.tags.length() == 0) {
					tag.deleteField(FieldKey.TAGS);
				} else {
					tag.setField(FieldKey.TAGS, aMp3Tag.tags);
				}
			}

			if (aMp3Tag.artwork.base64Data != null) {
				tag.deleteArtworkField();

				if (aMp3Tag.artwork.base64Data.length() > 0) {
					tag.addField(aMp3Tag.artwork.decode());
				}
			}

			mp3File.commit();
			mp3File.save();
		} catch (Exception error) {
			throw new AccelerateException(error);
		}
	}

	/**
	 * This method deletes the given tag from the Mp3 file
	 *
	 * @param aTrack
	 * @return Mp3Tag instance
	 * @throws AccelerateException
	 */
	public static Mp3Tag deleteV1Tag(File aTrack) throws AccelerateException {
		try {
			MP3File mp3File = ID3Util.getMP3File(aTrack);
			Tag tag = mp3File.getID3v1Tag();

			if (tag != null) {
				mp3File.delete(mp3File.getID3v1Tag());
				mp3File.commit();
				mp3File.save();
			}
		} catch (Exception error) {
			throw new AccelerateException(error);
		}

		return readTag(aTrack);
	}

	/**
	 * @param aTrack
	 * @return {@link TagCheckResult} instance
	 */
	public static TagCheckResult validateTrack(File aTrack) {
		TagCheckResult result = new TagCheckResult();
		result.setPassed(compare(getFileExtn(aTrack), "mp3"));

		if (!result.isPassed()) {
			if (!AppUtil.compareAny(getFileExtn(aTrack), "jpg", "db", "ini", "DS_Store")) {
				result.setReason("Unknown File Type: " + aTrack.getPath());
			}
		}

		return result;
	}

	/**
	 * @param aTrack
	 * @return {@link TagCheckResult} instance
	 * @throws AccelerateException
	 */
	public static TagCheckResult validateTag(File aTrack) throws AccelerateException {
		return validateTag(aTrack, standardFields);
	}

	/**
	 * @param aTrack
	 * @param aFieldKeys
	 * @return {@link TagCheckResult} instance
	 * @throws AccelerateException
	 */
	public static TagCheckResult validateTag(File aTrack, List<FieldKey> aFieldKeys) throws AccelerateException {
		TagCheckResult result = new TagCheckResult();
		result = checkTagFields(aTrack, aFieldKeys, false);

		if (result.isPassed()) {
			result = validateTagFields(aTrack);
		}

		return result;
	}

	/**
	 * @param aTrack
	 * @param aFieldKeys
	 * @param aRemoveInvalidFrames
	 * @return {@link TagCheckResult} instance
	 * @throws AccelerateException
	 */
	public static TagCheckResult checkTagFields(File aTrack, List<FieldKey> aFieldKeys, boolean aRemoveInvalidFrames)
			throws AccelerateException {
		TagCheckResult checkResult = new TagCheckResult();

		MP3File mp3File = getMP3File(aTrack);
		Tag audioTag = mp3File.getTag();

		if (audioTag == null) {
			checkResult.setReason("No Tag");
			return checkResult;
		}

		for (FieldKey key : aFieldKeys) {
			List<TagField> fieldList = audioTag.getFields(key);
			if (ObjectUtils.isEmpty(fieldList) || (fieldList.size() > 1)) {
				checkResult.setReason("Check Tag:" + key + "##" + aTrack.getPath());
				return checkResult;
			}
		}

		Iterator<TagField> fieldIterator = audioTag.getFields();
		Set<String> removeFrames = new HashSet<>();
		StringBuilder checkResultReason = new StringBuilder();
		checkResult.setPassed(true);

		while (fieldIterator.hasNext()) {
			TagField field = fieldIterator.next();
			if (standardFrames.get(field.getId()) != null) {
				continue;
			}

			if (!aRemoveInvalidFrames) {
				checkResultReason.append("Invalid Frame:" + field.getId() + ":" + field.toString() + "@@");
				checkResult.setPassed(false);
			} else {
				removeFrames.add(field.getId());
			}
		}

		if (!checkResult.isPassed()) {
			checkResult.setReason("Check Tag. " + checkResultReason.toString() + "##" + aTrack.getPath());
		}

		if (removeFrames.size() > 0) {
			for (String frameId : removeFrames) {
				audioTag.deleteField(frameId);
			}

			try {
				mp3File.commit();
				mp3File.save();
			} catch (CannotWriteException | IOException | TagException error) {
				throw new AccelerateException(error);
			}
		}

		return checkResult;
	}

	/**
	 * @param aTrack
	 * @return {@link TagCheckResult} instance
	 * @throws AccelerateException
	 */
	public static TagCheckResult validateTagFields(File aTrack) throws AccelerateException {
		TagCheckResult result = new TagCheckResult();
		Mp3Tag mp3Tag = readTag(aTrack);
		int parentIndex = 2;

		String titleName = FileUtil.getFileName(aTrack);
		if (!compare(mp3Tag.title, titleName)) {
			result.setReason("Mismatch Title:" + mp3Tag.title + "|" + titleName + "##" + aTrack.getPath());
			return result;
		}

		String albumName = getParentName(aTrack, 1);
		if (!compare(mp3Tag.album, albumName)) {
			String albumWithVolumeName = getParentName(aTrack, 2) + " " + getParentName(aTrack, 1);
			parentIndex++;

			if (!compare(mp3Tag.album, albumWithVolumeName)) {
				if (!compare(mp3Tag.album + " Album", albumWithVolumeName)) {
					result.setReason("Mismatch Album:" + mp3Tag.album + "|" + albumName + "##" + aTrack.getPath());
					return result;
				}
			}
		}

		String albumArtist = getParentName(aTrack, parentIndex++);
		if (!compare(mp3Tag.albumArtist, albumArtist)) {
			result.setReason(
					"Mismatch AlbumArtist:" + mp3Tag.albumArtist + "|" + albumArtist + "##" + aTrack.getPath());
			return result;
		}

		String genre = getParentName(aTrack, parentIndex++);
		if (!compare(mp3Tag.genre, genre)) {
			result.setReason("Mismatch Genre:" + mp3Tag.genre + "|" + genre + "##" + aTrack.getPath());
			return result;
		}

		String language = getParentName(aTrack, parentIndex++);
		if (!compare(mp3Tag.language, language)) {
			result.setReason("Mismatch Language:" + mp3Tag.language + "|" + language + "##" + aTrack.getPath());
			return result;
		}

		result.setPassed(true);
		return result;
	}

	/**
	 * @param aMainTag
	 * @param aThisTag
	 * @return
	 */
	public static TagCheckResult compareTags(Mp3Tag aMainTag, Mp3Tag aThisTag) {
		if ((aMainTag == null) || (aThisTag == null)) {
			return new TagCheckResult(false, aThisTag + "!=" + aMainTag);
		}

		int compareValue = aMainTag.compareTo(aThisTag);
		switch (compareValue) {
		case -1:
			return new TagCheckResult(false, "id: " + aThisTag.id + "!=" + aMainTag.id);
		case -2:
			return new TagCheckResult(false, "language: " + aThisTag.language + "!=" + aMainTag.language);
		case -3:
			return new TagCheckResult(false, "genre: " + aThisTag.genre + "!=" + aMainTag.genre);
		case -4:
			return new TagCheckResult(false, "mood: " + aThisTag.mood + "!=" + aMainTag.mood);
		case -5:
			return new TagCheckResult(false, "album: " + aThisTag.album + "!=" + aMainTag.album);
		case -6:
			return new TagCheckResult(false, "year: " + aThisTag.year + "!=" + aMainTag.year);
		case -7:
			return new TagCheckResult(false, "albumArtist: " + aThisTag.albumArtist + "!=" + aMainTag.albumArtist);
		case -8:
			return new TagCheckResult(false, "composer: " + aThisTag.composer + "!=" + aMainTag.composer);
		case -9:
			return new TagCheckResult(false, "artist: " + aThisTag.artist + "!=" + aMainTag.artist);
		case -10:
			return new TagCheckResult(false, "trackNbr: " + aThisTag.trackNbr + "!=" + aMainTag.trackNbr);
		case -11:
			return new TagCheckResult(false, "title: " + aThisTag.title + "!=" + aMainTag.title);
		case -12:
			return new TagCheckResult(false, "lyrics: " + aThisTag.lyrics + "!=" + aMainTag.lyrics);
		case -13:
			return new TagCheckResult(false, "tags: " + aThisTag.tags + "!=" + aMainTag.tags);
		case -14:
			return new TagCheckResult(false, "artwork");
		}

		return new TagCheckResult(true, EMPTY_STRING);
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

	/**
	 * @param aTrack
	 * @param aTokens
	 * @return {@link Mp3Tag} instance
	 * @throws AccelerateException
	 */
	public static Mp3Tag parseTag(File aTrack, List<String> aTokens) throws AccelerateException {
		String fileName = FileUtil.getFileName(aTrack);
		Mp3Tag mp3Tag = new Mp3Tag();
		Queue<String> fieldQueue = new ArrayDeque<>();

		for (String token : aTokens) {
			if (token.endsWith(">")) {
				token = StringUtils.substring(token, 0, -1);
				fieldQueue.add(token);
			} else {
				int index = fileName.indexOf(token);
				if (index < 0) {
					throw new AccelerateException("token not found:" + token);
				}

				if (token.length() == 0) {
					index = -1;
				}

				String field = fieldQueue.poll();
				String fieldValue = StringUtils.substring(fileName, 0, index);
				fileName = StringUtils.substring(fileName, index + token.length(), -1);

				if (compare(field, "ignore")) {
					continue;
				} else if (!isEmpty(fieldValue) && (field == null)) {
					System.err.println("no field for token: " + fieldValue);
				} else if (field != null) {
					ReflectionUtil.setFieldValue(Mp3Tag.class, mp3Tag, field, fieldValue);
				}
			}
		}

		if (!isEmpty(mp3Tag.trackNbr)) {
			mp3Tag.trackNbr = Integer.valueOf(mp3Tag.trackNbr).toString();
		}

		return mp3Tag;
	}

	/**
	 * @param aTrack
	 * @param aCommonTag
	 * @throws AccelerateException
	 */
	public static void extractCommonTag(File aTrack, Mp3Tag aCommonTag) throws AccelerateException {
		Mp3Tag mp3Tag = ID3Util.readTag(aTrack);

		if (aCommonTag == null) {
			return;
		}

		aCommonTag.language = (isEmpty(aCommonTag.language) || compare(aCommonTag.language, mp3Tag.language))
				? mp3Tag.language : EMPTY_STRING;

		aCommonTag.genre = (isEmpty(aCommonTag.genre) || compare(aCommonTag.genre, mp3Tag.genre)) ? mp3Tag.genre
				: EMPTY_STRING;

		aCommonTag.mood = (isEmpty(aCommonTag.mood) || compare(aCommonTag.mood, mp3Tag.mood)) ? mp3Tag.mood
				: EMPTY_STRING;

		aCommonTag.album = (isEmpty(aCommonTag.album) || compare(aCommonTag.album, mp3Tag.album)) ? mp3Tag.genre
				: EMPTY_STRING;

		aCommonTag.year = (isEmpty(aCommonTag.year) || compare(aCommonTag.year, mp3Tag.year)) ? mp3Tag.year
				: EMPTY_STRING;

		aCommonTag.albumArtist = (isEmpty(aCommonTag.albumArtist)
				|| compare(aCommonTag.albumArtist, mp3Tag.albumArtist)) ? mp3Tag.albumArtist : EMPTY_STRING;

		aCommonTag.composer = (isEmpty(aCommonTag.composer) || compare(aCommonTag.composer, mp3Tag.composer))
				? mp3Tag.composer : EMPTY_STRING;

		aCommonTag.artist = (isEmpty(aCommonTag.artist) || compare(aCommonTag.artist, mp3Tag.artist)) ? mp3Tag.artist
				: EMPTY_STRING;
	}

	/**
	 * Method to initialize standardFrames attribute
	 */
	private static void setupStandardFrames() {
		standardFrames = new HashMap<>();
		standardFrames.put(ID3v24Frames.FRAME_ID_INITIAL_KEY, FieldKey.KEY);
		standardFrames.put(ID3v24Frames.FRAME_ID_LANGUAGE, FieldKey.LANGUAGE);
		standardFrames.put(ID3v24Frames.FRAME_ID_GENRE, FieldKey.GENRE);
		standardFrames.put(ID3v24Frames.FRAME_ID_MOOD, FieldKey.MOOD);
		standardFrames.put(ID3v24Frames.FRAME_ID_ALBUM, FieldKey.ALBUM);
		standardFrames.put(ID3v24Frames.FRAME_ID_YEAR, FieldKey.YEAR);
		standardFrames.put(ID3v23Frames.FRAME_ID_V3_TYER, FieldKey.YEAR);
		standardFrames.put(ID3v24Frames.FRAME_ID_COMPOSER, FieldKey.COMPOSER);
		standardFrames.put(ID3v24Frames.FRAME_ID_ACCOMPANIMENT, FieldKey.ALBUM_ARTIST);
		standardFrames.put(ID3v24Frames.FRAME_ID_ARTIST, FieldKey.ARTIST);
		standardFrames.put(ID3v24Frames.FRAME_ID_TRACK, FieldKey.TRACK);
		standardFrames.put(ID3v24Frames.FRAME_ID_TITLE, FieldKey.TITLE);
		standardFrames.put(ID3v24Frames.FRAME_ID_UNSYNC_LYRICS, FieldKey.LYRICS);
		standardFrames.put(ID3v24Frames.FRAME_ID_USER_DEFINED_INFO, FieldKey.TAGS);
		standardFrames.put(ID3v24Frames.FRAME_ID_ATTACHED_PICTURE, FieldKey.COVER_ART);
	}

	/**
	 * Method to initialize standardFields attribute
	 */
	private static void setupStandardFields() {
		standardFields = new ArrayList<>();
		standardFields.add(FieldKey.KEY);
		standardFields.add(FieldKey.LANGUAGE);
		standardFields.add(FieldKey.GENRE);
		standardFields.add(FieldKey.MOOD);
		standardFields.add(FieldKey.ALBUM);
		standardFields.add(FieldKey.YEAR);
		standardFields.add(FieldKey.COMPOSER);
		standardFields.add(FieldKey.ALBUM_ARTIST);
		standardFields.add(FieldKey.ARTIST);
		standardFields.add(FieldKey.TRACK);
		standardFields.add(FieldKey.TITLE);
		standardFields.add(FieldKey.LYRICS);
		standardFields.add(FieldKey.TAGS);
		standardFields.add(FieldKey.COVER_ART);
	}

	/**
	 * @param aFileTrack
	 * @param aDBTrack
	 * @return {@link TagCheckResult} instance
	 * @throws AccelerateException
	 */
	public static TagCheckResult compareTags(File aFileTrack, Mp3Tag aDBTrack) throws AccelerateException {
		TagCheckResult result = new TagCheckResult();
		Mp3Tag mp3Tag = readTag(aFileTrack);

		try {
			compareFields("Language", mp3Tag.language, aDBTrack.language);
			compareFields("Genre", mp3Tag.genre, aDBTrack.genre);
			compareFields("Album", mp3Tag.album, aDBTrack.album);
			compareFields("Year", mp3Tag.year, aDBTrack.year);
			compareFields("Composer", mp3Tag.composer, aDBTrack.composer);
			compareFields("AlbumArtist", mp3Tag.albumArtist, aDBTrack.albumArtist);
			compareFields("TrackNbr", mp3Tag.trackNbr, aDBTrack.trackNbr);
			compareFields("Title", mp3Tag.title, aDBTrack.title);

			if (AppUtil.xor((mp3Tag.artwork.base64Data == null), (aDBTrack.artwork.base64Data == null))) {
				throw new FlowControlException("Mismatch Artwork:" + mp3Tag.artwork + "|" + aDBTrack.artwork);
			}

			result.setPassed(true);
		} catch (FlowControlException error) {
			result.setReason(error.getMessage() + "##" + aFileTrack.getPath() + "|" + aDBTrack.toJSON());
		}

		return result;
	}

	/**
	 * @param aType
	 * @param aValue1
	 * @param aValue2
	 * @throws FlowControlException
	 */
	private static void compareFields(String aType, Object aValue1, Object aValue2) throws FlowControlException {
		if (compare(aValue1, aValue2)) {
			return;
		}

		StringBuilder buffer = new StringBuilder();
		buffer.append("Mismatch ").append(aType).append(":").append(aValue1).append("|").append(aValue2);
		throw new FlowControlException(buffer.toString());
	}

	/**
	 * @param aTrack
	 * @param aRemoveInvalidFrames
	 * @return {@link TagCheckResult} instance
	 * @throws AccelerateException
	 */
	public static TagCheckResult validateTag(File aTrack, boolean aRemoveInvalidFrames) throws AccelerateException {
		return validateTag(aTrack, standardFields, aRemoveInvalidFrames);
	}

	/**
	 * @param aTrack
	 * @param aFieldKeys
	 * @param aRemoveInvalidFrames
	 * @return {@link TagCheckResult} instance
	 * @throws AccelerateException
	 */
	public static TagCheckResult validateTag(File aTrack, List<FieldKey> aFieldKeys, boolean aRemoveInvalidFrames)
			throws AccelerateException {
		TagCheckResult result = new TagCheckResult();
		result = checkTagFields(aTrack, aFieldKeys, aRemoveInvalidFrames);

		if (result.isPassed()) {
			result = validateTagFields(aTrack);
		}

		return result;
	}

	/**
	 * @param aTrack
	 * @return
	 */
	public static Mp3Tag tempTag(File aTrack) {
		List<String> tokens = parseTagExpression("<language>-<genre>-<album>-<year>-<artist>-<title>");

		Mp3Tag mp3Tag = parseTag(aTrack, tokens);
		mp3Tag.fileName = aTrack.getName();

		mp3Tag.composer = mp3Tag.artist;
		mp3Tag.albumArtist = mp3Tag.artist;

		Mp3Tag.Header header = mp3Tag.header;
		header.size = 0;
		header.length = 0;
		header.mode = "mode";
		header.bitrateType = "VBR";
		header.bitrate = "0";
		header.frequency = "frequency";

		return mp3Tag;
	}
}