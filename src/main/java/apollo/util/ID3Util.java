package apollo.util;

import static accelerate.util.AccelerateConstants.EMPTY_STRING;
import static accelerate.util.AppUtil.compare;
import static accelerate.util.AppUtil.compareAny;
import static accelerate.util.FileUtil.getFileExtn;
import static accelerate.util.FileUtil.getParentName;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.id3.AbstractTag;
import org.jaudiotagger.tag.id3.ID3v23Frames;
import org.jaudiotagger.tag.id3.ID3v23Tag;
import org.jaudiotagger.tag.id3.ID3v24Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.StandardArtwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import accelerate.exception.AccelerateException;
import accelerate.exception.FlowControlException;
import accelerate.util.AppUtil;
import accelerate.util.FileUtil;
import accelerate.util.ReflectionUtil;
import accelerate.util.StringUtil;
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
	public static List<FieldKey> standardFields = null;

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

			if (AppUtil.xor((mp3Tag.artwork.jatArtwork == null), (aDBTrack.artwork.jatArtwork == null))) {
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
	 * @return {@link TagCheckResult} instance
	 */
	public static TagCheckResult validateTrack(File aTrack) {
		TagCheckResult result = new TagCheckResult();
		result.setPassed(compare(getFileExtn(aTrack), "mp3"));

		if (!result.isPassed()) {
			if (!compareAny(getFileExtn(aTrack), "jpg", "db", "ini", "DS_Store")) {
				result.setReason("Unknown File Type: " + aTrack.getPath());
			}
		}

		return result;
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
	 * @param aFieldKeys
	 * @param aRemoveInvalidFrames
	 * @return {@link TagCheckResult} instance
	 * @throws AccelerateException
	 */
	public static TagCheckResult checkTagFields(File aTrack, List<FieldKey> aFieldKeys, boolean aRemoveInvalidFrames)
			throws AccelerateException {
		TagCheckResult checkResult = new TagCheckResult();

		Tag audioTag = getMP3Tag(aTrack);
		if (audioTag == null) {
			checkResult.setReason("No Tag");
			checkResult.setPassed(false);
			return checkResult;
		}

		for (FieldKey key : aFieldKeys) {
			List<TagField> fieldList = audioTag.getFields(key);
			if (isEmpty(fieldList) || fieldList.size() > 1) {
				checkResult.setReason("Check Tag:" + key + "##" + aTrack.getPath());
				return checkResult;
			}
		}

		Iterator<TagField> fieldIterator = audioTag.getFields();
		while (fieldIterator.hasNext()) {
			TagField field = fieldIterator.next();
			if (standardFrames.get(field.getId()) != null) {
				continue;
			}

			if (!aRemoveInvalidFrames) {
				try {
					checkResult.setReason("Check Tag. Invalid Frame:" + field.getId() + "@@"
							+ new String(field.getRawContent()) + "##" + aTrack.getPath());
				} catch (UnsupportedEncodingException error) {
					logger.warn("error in getting frame content:{}. error:{}", field.getId(), error.getMessage());
					checkResult.setReason("Check Tag. Invalid Frame:" + field.getId() + "@@N/A##" + aTrack.getPath());
				}

				return checkResult;
			}

			Mp3Tag mp3Tag = readTag(aTrack);
			writeTag(aTrack, mp3Tag, 2);
			break;
		}

		checkResult.setPassed(true);
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
		if (!compare(aThisTag.language, aMainTag.language) && !isEmpty(aMainTag.language)) {
			return new TagCheckResult(false, aThisTag.language + "!=" + aMainTag.language);
		}

		if (!compare(aThisTag.genre, aMainTag.genre) && !isEmpty(aMainTag.genre)) {
			return new TagCheckResult(false, aThisTag.genre + "!=" + aMainTag.genre);
		}

		if (!compare(aThisTag.album, aMainTag.album) && !isEmpty(aMainTag.album)) {
			return new TagCheckResult(false, aThisTag.album + "!=" + aMainTag.album);
		}

		if (!compare(aThisTag.year, aMainTag.year) && !isEmpty(aMainTag.year)) {
			return new TagCheckResult(false, aThisTag.year + "!=" + aMainTag.year);
		}

		if (!compare(aThisTag.composer, aMainTag.composer) && !isEmpty(aMainTag.composer)) {
			return new TagCheckResult(false, aThisTag.composer + "!=" + aMainTag.composer);
		}

		if (!compare(aThisTag.albumArtist, aMainTag.albumArtist) && !isEmpty(aMainTag.albumArtist)) {
			return new TagCheckResult(false, aThisTag.albumArtist + "!=" + aMainTag.albumArtist);
		}

		if (!compare(aThisTag.artist, aMainTag.artist) && !isEmpty(aMainTag.artist)) {
			return new TagCheckResult(false, aThisTag.artist + "!=" + aMainTag.artist);
		}

		if (!compare(aThisTag.trackNbr, aMainTag.trackNbr) && !isEmpty(aMainTag.trackNbr)) {
			return new TagCheckResult(false, aThisTag.trackNbr + "!=" + aMainTag.trackNbr);
		}

		if (!compare(aThisTag.title, aMainTag.title) && !isEmpty(aMainTag.title)) {
			return new TagCheckResult(false, aThisTag.title + "!=" + aMainTag.title);
		}

		if (aMainTag.artwork.jatArtwork != null && aThisTag.artwork.jatArtwork == null) {
			return new TagCheckResult(false, aThisTag.artwork + "!=" + aMainTag.artwork);
		}

		return new TagCheckResult(true, EMPTY_STRING);
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
	 * @return {@link Tag} instance
	 * @throws AccelerateException
	 */
	public static Tag getMP3Tag(File aTrack) throws AccelerateException {
		return getMP3File(aTrack).getTag();
	}

	/**
	 * @param aTrack
	 * @return {@link Mp3Tag} instance
	 * @throws AccelerateException
	 */
	public static Mp3Tag readTag(File aTrack) throws AccelerateException {
		Mp3Tag mp3Tag = new Mp3Tag();
		mp3Tag.sourceFile = aTrack;
		retrieveTag(mp3Tag);
		return mp3Tag;
	}

	/**
	 * @param aTrack
	 * @return
	 */
	public static Mp3Tag tempTag(File aTrack) {
		List<String> tokens = parseTagExpression("<language>-<genre>-<album>-<year>-<artist>-<title>");

		Mp3Tag mp3Tag = parseTag(aTrack, tokens);
		mp3Tag.sourceFile = aTrack;

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

	/**
	 * @param aMp3Tag
	 * @throws AccelerateException
	 */
	public static void retrieveTag(Mp3Tag aMp3Tag) throws AccelerateException {
		MP3File mp3File = getMP3File(aMp3Tag.sourceFile);
		Tag tag = mp3File.getTag();

		aMp3Tag.language = tag.getFirst(FieldKey.LANGUAGE);
		aMp3Tag.genre = tag.getFirst(FieldKey.GENRE);
		aMp3Tag.album = tag.getFirst(FieldKey.ALBUM);
		aMp3Tag.year = tag.getFirst(FieldKey.YEAR);
		aMp3Tag.composer = tag.getFirst(FieldKey.COMPOSER);
		aMp3Tag.albumArtist = tag.getFirst(FieldKey.ALBUM_ARTIST);
		aMp3Tag.artist = tag.getFirst(FieldKey.ARTIST);
		aMp3Tag.title = tag.getFirst(FieldKey.TITLE);
		aMp3Tag.trackNbr = tag.getFirst(FieldKey.TRACK);

		Artwork artwork = tag.getFirstArtwork();
		if (artwork != null) {
			aMp3Tag.artwork.jatArtwork = artwork;
		}

		AudioHeader audioHeader = mp3File.getAudioHeader();
		Mp3Tag.Header header = aMp3Tag.header;
		header.size = aMp3Tag.sourceFile.length();
		header.length = audioHeader.getTrackLength();
		header.mode = audioHeader.getChannels();
		header.bitrateType = audioHeader.isVariableBitRate() ? "VBR" : "CBR";
		header.bitrate = audioHeader.getBitRate();
		header.frequency = audioHeader.getSampleRate();
	}

	/**
	 * @param aPattern
	 * @return parsed tokens
	 */
	public static List<String> parseTagExpression(String aPattern) {
		return StringUtil.split(aPattern, "<").stream().filter(token -> StringUtils.isNotBlank(token))
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
				String fieldValue = StringUtil.extract(fileName, 0, index);
				fileName = StringUtil.extract(fileName, index + token.length(), -1);

				if (compare(field, "ignore")) {
					continue;
				} else if (!isEmpty(fieldValue) && field == null) {
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

		aCommonTag.album = (isEmpty(aCommonTag.album) || compare(aCommonTag.album, mp3Tag.album)) ? mp3Tag.genre
				: EMPTY_STRING;

		aCommonTag.year = (isEmpty(aCommonTag.year) || compare(aCommonTag.year, mp3Tag.year)) ? mp3Tag.year
				: EMPTY_STRING;

		aCommonTag.composer = (isEmpty(aCommonTag.composer) || compare(aCommonTag.composer, mp3Tag.composer))
				? mp3Tag.composer : EMPTY_STRING;

		aCommonTag.albumArtist = (isEmpty(aCommonTag.albumArtist)
				|| compare(aCommonTag.albumArtist, mp3Tag.albumArtist)) ? mp3Tag.albumArtist : EMPTY_STRING;

		aCommonTag.artist = (isEmpty(aCommonTag.artist) || compare(aCommonTag.artist, mp3Tag.artist)) ? mp3Tag.artist
				: EMPTY_STRING;

		aCommonTag.trackNbr = (isEmpty(aCommonTag.trackNbr) || compare(aCommonTag.trackNbr, mp3Tag.trackNbr))
				? mp3Tag.trackNbr : EMPTY_STRING;

		aCommonTag.title = (isEmpty(aCommonTag.title) || compare(aCommonTag.title, mp3Tag.title)) ? mp3Tag.title
				: EMPTY_STRING;

		aCommonTag.artwork.jatArtwork = (isEmpty(aCommonTag.artwork.jatArtwork)
				|| compare(aCommonTag.artwork.jatArtwork, mp3Tag.artwork.jatArtwork)) ? mp3Tag.artwork.jatArtwork
						: null;
	}

	/**
	 * @param aMainTag
	 * @param aOverrideTag
	 */
	public static void mergeTags(Mp3Tag aMainTag, Mp3Tag aOverrideTag) {
		if (!isEmpty(aOverrideTag.language)) {
			aMainTag.language = aOverrideTag.language;
		}

		if (!isEmpty(aOverrideTag.genre)) {
			aMainTag.genre = aOverrideTag.genre;
		}

		if (!isEmpty(aOverrideTag.album)) {
			aMainTag.album = aOverrideTag.album;
		}

		if (!isEmpty(aOverrideTag.year)) {
			aMainTag.year = aOverrideTag.year;
		}

		if (!isEmpty(aOverrideTag.composer)) {
			aMainTag.composer = aOverrideTag.composer;
		}

		if (!isEmpty(aOverrideTag.albumArtist)) {
			aMainTag.albumArtist = aOverrideTag.albumArtist;
		}

		if (!isEmpty(aOverrideTag.artist)) {
			aMainTag.artist = aOverrideTag.artist;
		}

		if (!isEmpty(aOverrideTag.trackNbr)) {
			aMainTag.trackNbr = aOverrideTag.trackNbr;
		}

		if (!isEmpty(aOverrideTag.title)) {
			aMainTag.title = aOverrideTag.title;
		}

		if (!isEmpty(aOverrideTag.artworkFile)) {
			aMainTag.artworkFile = aOverrideTag.artworkFile;
		}

		if (!isEmpty(aOverrideTag.artwork)) {
			aMainTag.artwork = aOverrideTag.artwork;
		}

	}

	/**
	 * @param aTrack
	 * @param mp3Tag
	 * @throws AccelerateException
	 */
	public static void writeTag(File aTrack, Mp3Tag mp3Tag) throws AccelerateException {
		writeTag(aTrack, mp3Tag, 0);
	}

	/**
	 * @param aTrack
	 * @param mp3Tag
	 * @param aWriteFlag
	 * @throws AccelerateException
	 */
	public static void writeTag(File aTrack, Mp3Tag mp3Tag, int aWriteFlag) throws AccelerateException {
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
				if (aWriteFlag == 0) {
					tag = new ID3v23Tag();
					mp3File.setTag(tag);
				} else {
					System.err.println("No Tag: " + aTrack.getPath());
					return;
				}
			}

			if (!isEmpty(mp3Tag.language)) {
				tag.setField(FieldKey.LANGUAGE, mp3Tag.language);
			}

			if (!isEmpty(mp3Tag.genre)) {
				tag.setField(FieldKey.GENRE, mp3Tag.genre);
			}

			if (!isEmpty(mp3Tag.album)) {
				tag.setField(FieldKey.ALBUM, mp3Tag.album);
			}

			if (!isEmpty(mp3Tag.year)) {
				tag.setField(FieldKey.YEAR, mp3Tag.year);
			}

			if (!isEmpty(mp3Tag.composer)) {
				tag.setField(FieldKey.COMPOSER, mp3Tag.composer);
			}

			if (!isEmpty(mp3Tag.albumArtist)) {
				tag.setField(FieldKey.ALBUM_ARTIST, mp3Tag.albumArtist);
			}

			if (!isEmpty(mp3Tag.artist)) {
				tag.setField(FieldKey.ARTIST, mp3Tag.artist);
			}

			if (!isEmpty(mp3Tag.title)) {
				tag.setField(FieldKey.TITLE, mp3Tag.title);
			}

			if (!isEmpty(mp3Tag.trackNbr)) {
				tag.setField(FieldKey.TRACK, mp3Tag.trackNbr);
			}

			if (!isEmpty(mp3Tag.artworkFile)) {
				tag.deleteArtworkField();
				tag.addField(StandardArtwork.createArtworkFromFile(mp3Tag.artworkFile));
			}

			if (!isEmpty(mp3Tag.artwork.jatArtwork)) {
				tag.deleteArtworkField();
				tag.addField(mp3Tag.artwork.jatArtwork);
			}

			mp3File.commit();
			mp3File.save();
		} catch (Exception error) {
			throw new AccelerateException(error);
		}
	}

	/**
	 * This method deletes the default tag from the Mp3 file
	 * 
	 * @param aTrack
	 * @throws AccelerateException
	 */
	public static void deleteTag(File aTrack) throws AccelerateException {
		Mp3Tag mp3Tag = readTag(aTrack);

		try {
			if (mp3Tag != null) {
				MP3File mp3File = ID3Util.getMP3File(aTrack);
				mp3File.delete((AbstractTag) mp3File.getTag());
				mp3File.commit();
				mp3File.save();
			}
		} catch (Exception error) {
			throw new AccelerateException(error);
		}
	}

	/**
	 * This method deletes the given tag from the Mp3 file
	 * 
	 * @param aTrack
	 * @throws AccelerateException
	 */
	public static void deleteV1Tag(File aTrack) throws AccelerateException {
		Mp3Tag mp3Tag = readTag(aTrack);

		try {
			if (mp3Tag != null) {
				MP3File mp3File = ID3Util.getMP3File(aTrack);
				mp3File.delete(mp3File.getID3v1Tag());
				mp3File.commit();
				mp3File.save();
			}
		} catch (Exception error) {
			throw new AccelerateException(error);
		}
	}

	/**
	 * Method to initialize standardFrames attribute
	 */
	private static void setupStandardFrames() {
		standardFrames = new HashMap<>();
		standardFrames.put(ID3v23Frames.FRAME_ID_V3_LANGUAGE, FieldKey.LANGUAGE);
		standardFrames.put(ID3v23Frames.FRAME_ID_V3_GENRE, FieldKey.GENRE);
		standardFrames.put(ID3v23Frames.FRAME_ID_V3_ALBUM, FieldKey.ALBUM);
		standardFrames.put(ID3v23Frames.FRAME_ID_V3_TYER, FieldKey.YEAR);
		standardFrames.put(ID3v23Frames.FRAME_ID_V3_COMPOSER, FieldKey.COMPOSER);
		standardFrames.put(ID3v23Frames.FRAME_ID_V3_ALBUM_ARTIST_SORT_ORDER_ITUNES, FieldKey.ALBUM_ARTIST);
		standardFrames.put(ID3v23Frames.FRAME_ID_V3_ARTIST, FieldKey.ARTIST);
		standardFrames.put(ID3v23Frames.FRAME_ID_V3_TITLE, FieldKey.TITLE);
		standardFrames.put(ID3v23Frames.FRAME_ID_V3_ATTACHED_PICTURE, FieldKey.COVER_ART);
		standardFrames.put(ID3v23Frames.FRAME_ID_V3_TRACK, FieldKey.TRACK);
	}

	/**
	 * Method to initialize standardFields attribute
	 */
	private static void setupStandardFields() {
		standardFields = new ArrayList<>();
		standardFields.add(FieldKey.LANGUAGE);
		standardFields.add(FieldKey.GENRE);
		standardFields.add(FieldKey.ALBUM);
		standardFields.add(FieldKey.YEAR);
		standardFields.add(FieldKey.COMPOSER);
		standardFields.add(FieldKey.ALBUM_ARTIST);
		standardFields.add(FieldKey.ARTIST);
		standardFields.add(FieldKey.TRACK);
		standardFields.add(FieldKey.TITLE);
		standardFields.add(FieldKey.COVER_ART);
	}
}