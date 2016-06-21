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
import java.util.Base64;
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
import org.jaudiotagger.tag.id3.ID3v1Tag;
import org.jaudiotagger.tag.id3.ID3v23Frames;
import org.jaudiotagger.tag.id3.ID3v24Frames;
import org.jaudiotagger.tag.id3.ID3v24Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.StandardArtwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import accelerate.exception.AccelerateException;
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
	protected static final Logger LOGGER = LoggerFactory.getLogger(ID3Util.class);

	/**
	 * {@link Map} containing standard frame names
	 */
	public static final String DELETE_FIELD_CONSTANT = "|~|";

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
		} catch (Exception error) {
			LOGGER.error(error.getMessage(), error);
		}
	}

	/**
	 * @param aTrack
	 * @return {@link MP3File} instance
	 * @throws AccelerateException
	 */
	private static MP3File getMP3File(File aTrack) throws AccelerateException {
		try {
			return new MP3File(aTrack);
		} catch (IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException error) {
			throw new AccelerateException(error);
		}
	}

	/**
	 * @param aTrack
	 * @param aTestMode
	 * @return {@link Mp3Tag} instance
	 * @throws AccelerateException
	 */
	public static Mp3Tag readTag(File aTrack, boolean aTestMode) throws AccelerateException {
		/*
		 * TODO: temporary check for testing.
		 */
		if (aTestMode) {
			List<String> tokens = parseTagExpression("<language>-<genre>-<album>-<year>-<artist>-<title>");

			Mp3Tag mp3Tag = parseTag(aTrack, tokens);
			mp3Tag.fileName = FileUtil.getFileName(aTrack);
			mp3Tag.filePath = FileUtil.getFilePath(aTrack);

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

		MP3File mp3File = getMP3File(aTrack);
		Tag tag = mp3File.getTag();

		Mp3Tag mp3Tag = new Mp3Tag();
		mp3Tag.fileName = FileUtil.getFileName(aTrack);
		mp3Tag.filePath = FileUtil.getFilePath(aTrack);
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
			mp3Tag.artwork = Base64.getEncoder().encodeToString(artwork.getBinaryData());
		}

		AudioHeader audioHeader = mp3File.getAudioHeader();
		mp3Tag.header.size = aTrack.length();
		mp3Tag.header.length = audioHeader.getTrackLength();
		mp3Tag.header.mode = audioHeader.getChannels();
		mp3Tag.header.bitrateType = audioHeader.isVariableBitRate() ? "VBR" : "CBR";
		mp3Tag.header.bitrate = audioHeader.getBitRate();
		mp3Tag.header.frequency = audioHeader.getSampleRate();

		if (StringUtils.isEmpty(mp3Tag.id)) {
			mp3Tag.id = String.valueOf(aTrack.getPath().hashCode());
		}

		return mp3Tag;
	}

	/**
	 * @param aTracks
	 * @param aTestMode
	 * @return
	 * @throws AccelerateException
	 */
	public static Mp3Tag readCommonTag(List<File> aTracks, boolean aTestMode) throws AccelerateException {
		Mp3Tag commonTag = new Mp3Tag();
		aTracks.stream().parallel().forEach(aTrack -> {
			Mp3Tag mp3Tag = ID3Util.readTag(aTrack, aTestMode);
			extractCommonTag(commonTag, mp3Tag);
		});

		return commonTag;
	}

	/**
	 * @param aCommonTag
	 * @param aMp3Tag
	 * @throws AccelerateException
	 */
	public static void extractCommonTag(Mp3Tag aCommonTag, Mp3Tag aMp3Tag) throws AccelerateException {
		if (!AppUtil.compare(aCommonTag.get("initialized"), "true")) {
			aCommonTag.language = aMp3Tag.language;
			aCommonTag.genre = aMp3Tag.genre;
			aCommonTag.mood = aMp3Tag.mood;
			aCommonTag.album = aMp3Tag.album;
			aCommonTag.year = aMp3Tag.year;
			aCommonTag.albumArtist = aMp3Tag.albumArtist;
			aCommonTag.composer = aMp3Tag.composer;
			aCommonTag.artist = aMp3Tag.artist;
			aCommonTag.tags = aMp3Tag.tags;
			aCommonTag.put("initialized", "true");
			return;
		}

		aCommonTag.language = compare(aCommonTag.language, aMp3Tag.language) ? aMp3Tag.language : EMPTY_STRING;
		aCommonTag.genre = compare(aCommonTag.genre, aMp3Tag.genre) ? aMp3Tag.genre : EMPTY_STRING;
		aCommonTag.mood = compare(aCommonTag.mood, aMp3Tag.mood) ? aMp3Tag.mood : EMPTY_STRING;
		aCommonTag.album = compare(aCommonTag.album, aMp3Tag.album) ? aMp3Tag.album : EMPTY_STRING;
		aCommonTag.year = compare(aCommonTag.year, aMp3Tag.year) ? aMp3Tag.year : EMPTY_STRING;
		aCommonTag.albumArtist = compare(aCommonTag.albumArtist, aMp3Tag.albumArtist) ? aMp3Tag.albumArtist
				: EMPTY_STRING;
		aCommonTag.composer = compare(aCommonTag.composer, aMp3Tag.composer) ? aMp3Tag.composer : EMPTY_STRING;
		aCommonTag.artist = compare(aCommonTag.artist, aMp3Tag.artist) ? aMp3Tag.artist : EMPTY_STRING;
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
	 * @param aTestMode
	 * @throws AccelerateException
	 */
	public static void writeTag(File aTrack, Mp3Tag mp3Tag, boolean aTestMode) throws AccelerateException {
		writeTag(aTrack, mp3Tag, aTestMode ? 6 : 0);
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
	 *            <li>0: Write into available tag, create a new if no tag is
	 *            present</li>
	 *            <li>1: Write into available tag else ignore</li>
	 *            <li>2: Write into available tag else throw an exeption is tag
	 *            not available</li>
	 *            <li>3: Write into a new tag, ignore if tag already
	 *            present</li>
	 *            <li>4: Write into a new tag, throw an exception if tag already
	 *            present</li>
	 *            <li>5: Always write into a new tag, delete if a tag is
	 *            present</li>
	 *            <li>6: Do not do anything. Test mode</li>
	 *            </ul>
	 * @throws AccelerateException
	 */
	public static void writeTag(File aTrack, Mp3Tag aMp3Tag, int aWriteFlag) throws AccelerateException {
		try {
			if (aWriteFlag == 6) {
				LOGGER.info("Test mode active. Logging tag [{}]", aMp3Tag);
				return;
			}

			MP3File mp3File = getMP3File(aTrack);
			Tag tag = mp3File.getTag();

			if (aWriteFlag == 5) {
				if (tag != null) {
					mp3File.delete((AbstractTag) tag);
				}

				tag = new ID3v24Tag();
				mp3File.setTag(tag);
			}

			if (tag == null) {
				if (aWriteFlag == 1) {
					LOGGER.debug("No Tag available for [{}]", aTrack);
					return;
				}

				if (aWriteFlag == 2) {
					throw new AccelerateException("No Tag available for [%s]", aTrack);
				}

				tag = new ID3v24Tag();
				mp3File.setTag(tag);
			} else {
				if (aWriteFlag == 3) {
					LOGGER.debug("Tag already present for [{}]", aTrack);
					return;
				}

				if (aWriteFlag == 4) {
					throw new AccelerateException("Tag already present for [%s]", aTrack);
				}
			}

			/*
			 * Delete any v1 tag if present
			 */
			ID3v1Tag v1Tag = mp3File.getID3v1Tag();
			if (v1Tag != null) {
				LOGGER.debug("Deleting v1 tag for [{}]", aTrack);
				mp3File.delete(v1Tag);
			}

			/*
			 * Start writing the tag
			 */
			if (aMp3Tag.id != null) {
				if (AppUtil.compare(aMp3Tag.id, DELETE_FIELD_CONSTANT)) {
					tag.deleteField(FieldKey.KEY);
				} else {
					tag.setField(FieldKey.KEY, aMp3Tag.id);
				}
			}

			if (aMp3Tag.language != null) {
				if (AppUtil.compare(aMp3Tag.language, DELETE_FIELD_CONSTANT)) {
					tag.deleteField(FieldKey.LANGUAGE);
				} else {
					tag.setField(FieldKey.LANGUAGE, aMp3Tag.language);
				}
			}

			if (aMp3Tag.genre != null) {
				if (AppUtil.compare(aMp3Tag.genre, DELETE_FIELD_CONSTANT)) {
					tag.deleteField(FieldKey.GENRE);
				} else {
					tag.setField(FieldKey.GENRE, aMp3Tag.genre);
				}
			}

			if (aMp3Tag.mood != null) {
				if (AppUtil.compare(aMp3Tag.mood, DELETE_FIELD_CONSTANT)) {
					tag.deleteField(FieldKey.MOOD);
				} else {
					tag.setField(FieldKey.MOOD, aMp3Tag.mood);
				}
			}

			if (aMp3Tag.album != null) {
				if (AppUtil.compare(aMp3Tag.album, DELETE_FIELD_CONSTANT)) {
					tag.deleteField(FieldKey.ALBUM);
				} else {
					tag.setField(FieldKey.ALBUM, aMp3Tag.album);
				}
			}

			if (aMp3Tag.year != null) {
				if (AppUtil.compare(aMp3Tag.year, DELETE_FIELD_CONSTANT)) {
					tag.deleteField(FieldKey.YEAR);
				} else {
					tag.setField(FieldKey.YEAR, aMp3Tag.year);
				}
			}

			if (aMp3Tag.albumArtist != null) {
				if (AppUtil.compare(aMp3Tag.albumArtist, DELETE_FIELD_CONSTANT)) {
					tag.deleteField(FieldKey.ALBUM_ARTIST);
				} else {
					tag.setField(FieldKey.ALBUM_ARTIST, aMp3Tag.albumArtist);
				}
			}

			if (aMp3Tag.composer != null) {
				if (AppUtil.compare(aMp3Tag.composer, DELETE_FIELD_CONSTANT)) {
					tag.deleteField(FieldKey.COMPOSER);
				} else {
					tag.setField(FieldKey.COMPOSER, aMp3Tag.composer);
				}
			}

			if (aMp3Tag.artist != null) {
				if (AppUtil.compare(aMp3Tag.artist, DELETE_FIELD_CONSTANT)) {
					tag.deleteField(FieldKey.ARTIST);
				} else {
					tag.setField(FieldKey.ARTIST, aMp3Tag.artist);
				}
			}

			if (aMp3Tag.trackNbr != null) {
				tag.deleteField(FieldKey.TRACK_TOTAL);
				if (AppUtil.compare(aMp3Tag.trackNbr, DELETE_FIELD_CONSTANT)) {
					tag.deleteField(FieldKey.TRACK);
				} else {
					tag.setField(FieldKey.TRACK, aMp3Tag.trackNbr);
				}
			}

			if (aMp3Tag.title != null) {
				if (AppUtil.compare(aMp3Tag.title, DELETE_FIELD_CONSTANT)) {
					tag.deleteField(FieldKey.TITLE);
				} else {
					tag.setField(FieldKey.TITLE, aMp3Tag.title);
				}
			}

			if (aMp3Tag.lyrics != null) {
				if (AppUtil.compare(aMp3Tag.lyrics, DELETE_FIELD_CONSTANT)) {
					tag.deleteField(FieldKey.LYRICS);
				} else {
					tag.setField(FieldKey.LYRICS, aMp3Tag.lyrics);
				}
			}

			if (aMp3Tag.tags != null) {
				if (AppUtil.compare(aMp3Tag.tags, DELETE_FIELD_CONSTANT)) {
					tag.deleteField(FieldKey.TAGS);
				} else {
					tag.setField(FieldKey.TAGS, aMp3Tag.tags);
				}
			}

			if (aMp3Tag.artwork != null) {
				tag.deleteArtworkField();

				if (AppUtil.compare(aMp3Tag.artwork, DELETE_FIELD_CONSTANT)) {
					Artwork artwork = new StandardArtwork();
					artwork.setBinaryData(Base64.getDecoder().decode(aMp3Tag.artwork));
					tag.addField(artwork);
				}
			}

			mp3File.commit();
			mp3File.save();
		} catch (Exception error) {
			throw new AccelerateException(error);
		}
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
		Mp3Tag mp3Tag = readTag(aTrack, false);
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

				if (StringUtils.isEmpty(token)) {
					index = fileName.length();
				}

				if (index < 0) {
					throw new AccelerateException("token not found:" + token);
				}

				String field = fieldQueue.poll();
				String fieldValue = StringUtils.substring(fileName, 0, index);
				fileName = StringUtils.substring(fileName, index + token.length());

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
}