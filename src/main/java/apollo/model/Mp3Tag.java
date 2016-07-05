package apollo.model;

import static accelerate.util.AccelerateConstants.EMPTY_STRING;
import static accelerate.util.AppUtil.compare;
import static apollo.util.ApolloConstants.DELETE_FIELD_CONSTANT;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
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
import org.jaudiotagger.tag.id3.valuepair.ImageFormats;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.StandardArtwork;
import org.jaudiotagger.tag.reference.PictureTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import accelerate.databean.AccelerateDataBean;
import accelerate.exception.AccelerateException;
import accelerate.util.AccelerateConstants;
import accelerate.util.AppUtil;
import accelerate.util.FileUtil;
import accelerate.util.JSONUtil;
import apollo.util.Mp3TagUtil;

/**
 * Model class holding Mp3 tag information
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since December 13, 2010
 */
public class Mp3Tag extends AccelerateDataBean implements Comparable<Mp3Tag> {
	/**
	 * {@link Logger} instance
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Mp3Tag.class);

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * {@link Map} containing standard frame names
	 */
	private static List<FieldKey> standardFields = new ArrayList<>();

	/**
	 * {@link Map} containing standard frame names
	 */
	private static Map<String, FieldKey> standardFrames = new HashMap<>();

	/**
	 * {@link Header} information
	 */
	private Header header = null;

	/**
	 * Id
	 */
	private String id;

	/**
	 * Language
	 */
	private String language = null;

	/**
	 * Genre
	 */
	private String genre;

	/**
	 * Mood
	 */
	private String mood;

	/**
	 * Album
	 */
	private String album;

	/**
	 * Album Year
	 */
	private String year;

	/**
	 * Album Artist
	 */
	private String albumArtist;

	/**
	 * Album Composer
	 */
	private String composer;

	/**
	 * Track Artist
	 */
	private String artist;

	/**
	 * Track Number
	 */
	private String trackNbr;

	/**
	 * Track Title
	 */
	private String title;

	/**
	 * Track Lyrics
	 */
	private String lyrics;

	/**
	 * Tags
	 */
	private String tags;

	/**
	 * Album Artwork
	 */
	private String artwork;

	/**
	 * Name of the file
	 */
	private String fileName;

	/**
	 * Path of the file
	 */
	private String filePath = null;

	/**
	 * Erros in the tag
	 */
	private List<String> tagErrors = Collections.EMPTY_LIST;

	/**
	 * default constructor to allow empty instances
	 */
	public Mp3Tag() {
		addJsonIgnoreFields("header", "artwork", "tagCheckResult");
	}

	/**
	 * primary constructor for reading tag information
	 * 
	 * @param aTrack
	 */
	public Mp3Tag(File aTrack) {
		this();

		LOGGER.debug("Loading tag from [{}]", aTrack);

		this.fileName = FileUtil.getFileName(aTrack);
		this.filePath = FileUtil.getFilePath(aTrack);

		MP3File mp3File = Mp3TagUtil.getMP3File(aTrack);
		Tag tag = mp3File.getTag();
		AudioHeader audioHeader = mp3File.getAudioHeader();

		this.header = new Header(audioHeader, aTrack.length());

		this.id = tag.getFirst(FieldKey.KEY);
		this.language = tag.getFirst(FieldKey.LANGUAGE);
		this.genre = tag.getFirst(FieldKey.GENRE);
		this.mood = tag.getFirst(FieldKey.MOOD);
		this.album = tag.getFirst(FieldKey.ALBUM);
		this.year = tag.getFirst(FieldKey.YEAR);
		this.albumArtist = tag.getFirst(FieldKey.ALBUM_ARTIST);
		this.composer = tag.getFirst(FieldKey.COMPOSER);
		this.artist = tag.getFirst(FieldKey.ARTIST);
		this.trackNbr = tag.getFirst(FieldKey.TRACK);
		this.title = tag.getFirst(FieldKey.TITLE);
		this.lyrics = tag.getFirst(FieldKey.LYRICS);
		this.tags = tag.getFirst(FieldKey.TAGS);

		Artwork _artwork = tag.getFirstArtwork();
		if (_artwork != null) {
			this.artwork = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(_artwork.getBinaryData());
		}

		if (StringUtils.isEmpty(this.id)) {
			this.id = String.valueOf(aTrack.getPath().hashCode());
		}

		for (FieldKey key : standardFields) {
			List<TagField> fieldList = tag.getFields(key);
			if (ObjectUtils.isEmpty(fieldList) || (fieldList.size() > 1)) {
				addError("Invalid value for [" + key + "]  [" + JSONUtil.serialize(fieldList) + "]");
			}
		}
	}

	/**
	 * @param aTrack
	 * @param aTokens
	 */
	public Mp3Tag(File aTrack, List<String> aTokens) {
		this();

		LOGGER.debug("Parsing tag from [{}]", aTrack);

		this.fileName = FileUtil.getFileName(aTrack);
		this.filePath = FileUtil.getFilePath(aTrack);

		String parseString = this.fileName;
		Queue<String> fieldQueue = new ArrayDeque<>(8);

		for (String token : aTokens) {
			if (token.endsWith(">")) {
				fieldQueue.add(StringUtils.substring(token, 0, -1));
			} else {
				int index = parseString.indexOf(token);
				if (index < 0) {
					throw new AccelerateException("token not found:" + token);
				}

				/*
				 * If it is the empty token created at the end while parsing the
				 * tokens, then set the field value to remaining string
				 */
				if (AppUtil.compare(token, AccelerateConstants.EMPTY_STRING) && index == 0) {
					index = parseString.length();
				}

				String field = fieldQueue.poll();
				String fieldValue = StringUtils.substring(parseString, 0, index);
				parseString = StringUtils.substring(parseString, index + token.length());

				if (!isEmpty(fieldValue) && (field == null)) {
					LOGGER.info("Discarding token [{}] for no mapped field", fieldValue);
				} else if (field != null) {
					setField(field, fieldValue);
				}
			}
		}

		// Check if there is remaining field to be mapped
		if (fieldQueue.size() > 0) {
			throw new AccelerateException("[%s] unmapped fields remaining", fieldQueue.size());
		}

	}

	/**
	 * copy constructor
	 * 
	 * @param aMp3Tag
	 */
	public Mp3Tag(Mp3Tag aMp3Tag) {
		this();

		LOGGER.debug("Copying tag from [{}]", aMp3Tag);
		copyFrom(aMp3Tag);
	}

	/**
	 * copy constructor
	 * 
	 * @param aMp3Tag
	 */
	public void copyFrom(Mp3Tag aMp3Tag) {
		this.id = StringUtils.defaultString(aMp3Tag.id, this.id);
		this.language = StringUtils.defaultString(aMp3Tag.language, this.language);
		this.genre = StringUtils.defaultString(aMp3Tag.genre, this.genre);
		this.mood = StringUtils.defaultString(aMp3Tag.mood, this.mood);
		this.album = StringUtils.defaultString(aMp3Tag.album, this.album);
		this.year = StringUtils.defaultString(aMp3Tag.year, this.year);
		this.albumArtist = StringUtils.defaultString(aMp3Tag.albumArtist, this.albumArtist);
		this.composer = StringUtils.defaultString(aMp3Tag.composer, this.composer);
		this.artist = StringUtils.defaultString(aMp3Tag.artist, this.artist);
		this.trackNbr = StringUtils.defaultString(aMp3Tag.trackNbr, this.trackNbr);
		this.title = StringUtils.defaultString(aMp3Tag.title, this.title);
		this.lyrics = StringUtils.defaultString(aMp3Tag.lyrics, this.lyrics);
		this.tags = StringUtils.defaultString(aMp3Tag.tags, this.tags);
		this.artwork = StringUtils.defaultString(aMp3Tag.artwork, this.artwork);
	}

	/**
	 * @param aMp3Tag
	 * @throws AccelerateException
	 */
	public void extractCommonTag(Mp3Tag aMp3Tag) throws AccelerateException {
		String[] fieldList = new String[] { "language", "genre", "mood", "album", "year", "albumArtist", "composer",
				"artist", "tags", "artwork" };

		for (String field : fieldList) {
			if (!AppUtil.compare(get("initialized"), "true")) {
				setField(field, aMp3Tag.getField(field));
				continue;
			}

			if (!compare(getField(field), aMp3Tag.getField(field))) {
				setField(field, EMPTY_STRING);
			}
		}

		put("initialized", "true");
	}

	/**
	 * @param aFieldName
	 * @return
	 */
	private String getField(String aFieldName) {
		switch (aFieldName) {
		case "id":
			return this.id;
		case "language":
			return this.language;
		case "genre":
			return this.genre;
		case "mood":
			return this.mood;
		case "album":
			return this.album;
		case "year":
			return this.year;
		case "albumArtist":
			return this.albumArtist;
		case "composer":
			return this.composer;
		case "artist":
			return this.artist;
		case "title":
			return this.title;
		case "trackNbr":
			return this.trackNbr;
		case "tags":
			return this.tags;
		case "lyrics":
			return this.lyrics;
		case "artwork":
			return this.artwork;
		default:
			throw new AccelerateException("unknown field [%s]", aFieldName);
		}
	}

	/**
	 * @param aFieldName
	 * @param aFieldValue
	 */
	public void setField(String aFieldName, String aFieldValue) {
		String _value = StringUtils.isEmpty(aFieldValue) ? null : aFieldValue;

		LOGGER.debug("Setting field [{}] as [{}]", aFieldName, aFieldValue);

		switch (aFieldName) {
		case "id":
			this.id = _value;
			break;
		case "language":
			this.language = _value;
			break;
		case "genre":
			this.genre = _value;
			break;
		case "mood":
			this.mood = _value;
			break;
		case "album":
			this.album = _value;
			break;
		case "year":
			this.year = _value;
			break;
		case "albumArtist":
			this.albumArtist = _value;
			break;
		case "composer":
			this.composer = _value;
			break;
		case "artist":
			this.artist = _value;
			break;
		case "title":
			this.title = _value;
			break;
		case "trackNbr":
			this.trackNbr = Integer.valueOf(_value).toString();
			break;
		case "tags":
			this.tags = _value;
			break;
		case "lyrics":
			this.lyrics = _value;
			break;
		case "artwork":
			this.artwork = _value;
			break;
		case "filePath":
			this.filePath = _value;
			break;
		default:
			throw new AccelerateException("unknown field [%s]", aFieldName);
		}
	}

	/**
	 * @param aError
	 */
	public void addError(String aError) {
		if (this.tagErrors == Collections.EMPTY_LIST) {
			this.tagErrors = new ArrayList<>();
		}

		this.tagErrors.add(aError);
	}

	/**
	 * This is the default method to write the tag to the given file. It calls
	 * the {@link #save(int, File)} method with aSaveFlag argument as 0
	 *
	 * @throws AccelerateException
	 */
	public void save() throws AccelerateException {
		save(0, new File(this.filePath));
	}

	/**
	 * This method writes the tag to the given file
	 *
	 * @param aSaveFlag
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
	 * @param aTrackFile
	 *            File to which the track has to be written to
	 * @throws AccelerateException
	 */
	public void save(int aSaveFlag, File aTrackFile) throws AccelerateException {
		try {
			if (aSaveFlag == 6) {
				LOGGER.info("Test mode active. Logging tag [{}]", this);
				return;
			}

			MP3File mp3File = Mp3TagUtil.getMP3File(aTrackFile);
			Tag tag = mp3File.getTag();

			if (aSaveFlag == 5) {
				if (tag != null) {
					mp3File.delete((AbstractTag) tag);
				}

				tag = new ID3v24Tag();
				mp3File.setTag(tag);
			}

			if (tag == null) {
				if (aSaveFlag == 1) {
					LOGGER.debug("No Tag available for [{}]", this.filePath);
					return;
				}

				if (aSaveFlag == 2) {
					throw new AccelerateException("No Tag available for [%s]", this.filePath);
				}

				tag = new ID3v24Tag();
				mp3File.setTag(tag);
			} else {
				if (aSaveFlag == 3) {
					LOGGER.debug("Tag already present for [{}]", this.filePath);
					return;
				}

				if (aSaveFlag == 4) {
					throw new AccelerateException("Tag already present for [%s]", this.filePath);
				}
			}

			/*
			 * Delete any v1 tag if present
			 */
			ID3v1Tag v1Tag = mp3File.getID3v1Tag();
			if (v1Tag != null) {
				LOGGER.debug("Deleting v1 tag for [{}]", this.filePath);
				mp3File.delete(v1Tag);
			}

			/*
			 * Start writing the tag
			 */
			if (this.id != null) {
				if (AppUtil.compare(this.id, DELETE_FIELD_CONSTANT)) {
					tag.deleteField(FieldKey.KEY);
				} else {
					tag.setField(FieldKey.KEY, this.id);
				}
			}

			if (this.language != null) {
				if (AppUtil.compare(this.language, DELETE_FIELD_CONSTANT)) {
					tag.deleteField(FieldKey.LANGUAGE);
				} else {
					tag.setField(FieldKey.LANGUAGE, this.language);
				}
			}

			if (this.genre != null) {
				if (AppUtil.compare(this.genre, DELETE_FIELD_CONSTANT)) {
					tag.deleteField(FieldKey.GENRE);
				} else {
					tag.setField(FieldKey.GENRE, this.genre);
				}
			}

			if (this.mood != null) {
				if (AppUtil.compare(this.mood, DELETE_FIELD_CONSTANT)) {
					tag.deleteField(FieldKey.MOOD);
				} else {
					tag.setField(FieldKey.MOOD, this.mood);
				}
			}

			if (this.album != null) {
				if (AppUtil.compare(this.album, DELETE_FIELD_CONSTANT)) {
					tag.deleteField(FieldKey.ALBUM);
				} else {
					tag.setField(FieldKey.ALBUM, this.album);
				}
			}

			if (this.year != null) {
				if (AppUtil.compare(this.year, DELETE_FIELD_CONSTANT)) {
					tag.deleteField(FieldKey.YEAR);
				} else {
					tag.setField(FieldKey.YEAR, this.year);
				}
			}

			if (this.albumArtist != null) {
				if (AppUtil.compare(this.albumArtist, DELETE_FIELD_CONSTANT)) {
					tag.deleteField(FieldKey.ALBUM_ARTIST);
				} else {
					tag.setField(FieldKey.ALBUM_ARTIST, this.albumArtist);
				}
			}

			if (this.composer != null) {
				if (AppUtil.compare(this.composer, DELETE_FIELD_CONSTANT)) {
					tag.deleteField(FieldKey.COMPOSER);
				} else {
					tag.setField(FieldKey.COMPOSER, this.composer);
				}
			}

			if (this.artist != null) {
				if (AppUtil.compare(this.artist, DELETE_FIELD_CONSTANT)) {
					tag.deleteField(FieldKey.ARTIST);
				} else {
					tag.setField(FieldKey.ARTIST, this.artist);
				}
			}

			if (this.trackNbr != null) {
				if (AppUtil.compare(this.trackNbr, DELETE_FIELD_CONSTANT)) {
					tag.deleteField(FieldKey.TRACK);
				} else {
					tag.setField(FieldKey.TRACK, this.trackNbr);
				}
			}

			if (this.title != null) {
				if (AppUtil.compare(this.title, DELETE_FIELD_CONSTANT)) {
					tag.deleteField(FieldKey.TITLE);
				} else {
					tag.setField(FieldKey.TITLE, this.title);
				}
			}

			if (this.lyrics != null) {
				if (AppUtil.compare(this.lyrics, DELETE_FIELD_CONSTANT)) {
					tag.deleteField(FieldKey.LYRICS);
				} else {
					tag.setField(FieldKey.LYRICS, this.lyrics);
				}
			}

			if (this.tags != null) {
				if (AppUtil.compare(this.tags, DELETE_FIELD_CONSTANT)) {
					tag.deleteField(FieldKey.TAGS);
				} else {
					tag.setField(FieldKey.TAGS, this.tags);
				}
			}

			if (this.artwork != null) {
				tag.deleteArtworkField();

				if (!AppUtil.compare(this.artwork, DELETE_FIELD_CONSTANT)) {
					byte[] imageData = Base64.getDecoder()
							.decode(this.artwork.split(AccelerateConstants.COMMA_CHAR)[1]);

					Artwork _artwork = new StandardArtwork();
					_artwork.setBinaryData(imageData);
					_artwork.setMimeType(ImageFormats.getMimeTypeForBinarySignature(imageData));
					_artwork.setPictureType(PictureTypes.DEFAULT_ID);

					tag.addField(_artwork);
				}
			}

			/*
			 * Delete invalid tags
			 */
			Iterator<TagField> fieldIterator = tag.getFields();
			List<String> extraFrames = new ArrayList<>();
			while (fieldIterator.hasNext()) {
				TagField field = fieldIterator.next();
				if (standardFrames.get(field.getId()) != null) {
					continue;
				}

				extraFrames.add(field.getId());
			}

			/*
			 * Removing unneccessary fields
			 */
			for (String frameId : extraFrames) {
				LOGGER.debug("Removing invalid frame [{}] with value [{}]", frameId, tag.getFields(frameId));
				tag.deleteField(frameId);
			}
			tag.deleteField(FieldKey.GROUPING);
			tag.deleteField(FieldKey.TRACK_TOTAL);
			tag.deleteField(FieldKey.DISC_NO);
			tag.deleteField(FieldKey.DISC_TOTAL);
			tag.deleteField(FieldKey.COMMENT);

			mp3File.commit();
			mp3File.save();
		} catch (IOException | CannotWriteException | TagException error) {
			throw new AccelerateException(error);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	/**
	 * @param aMp3Tag
	 * @return
	 */
	@Override
	public int compareTo(Mp3Tag aMp3Tag) {
		if (!compare(aMp3Tag.id, this.id)) {
			return -1;
		}

		if (!compare(aMp3Tag.language, this.language)) {
			return -2;
		}

		if (!compare(aMp3Tag.genre, this.genre)) {
			return -3;
		}

		if (!compare(aMp3Tag.mood, this.mood)) {
			return -4;
		}

		if (!compare(aMp3Tag.album, this.album)) {
			return -5;
		}

		if (!compare(aMp3Tag.year, this.year)) {
			return -6;
		}

		if (!compare(aMp3Tag.albumArtist, this.albumArtist)) {
			return -7;
		}

		if (!compare(aMp3Tag.composer, this.composer)) {
			return -8;
		}

		if (!compare(aMp3Tag.artist, this.artist)) {
			return -9;
		}

		if (!compare(aMp3Tag.trackNbr, this.trackNbr)) {
			return -10;
		}

		if (!compare(aMp3Tag.title, this.title)) {
			return -11;
		}

		if (!compare(aMp3Tag.lyrics, this.lyrics)) {
			return -12;
		}

		if (!compare(aMp3Tag.tags, this.tags)) {
			return -13;
		}

		if (!compare(aMp3Tag.artwork, this.artwork)) {
			return -14;
		}

		return 0;
	}

	/**
	 * Getter method for "header" property
	 * 
	 * @return header
	 */
	public Header getHeader() {
		return this.header;
	}

	/**
	 * Getter method for "id" property
	 * 
	 * @return id
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Getter method for "language" property
	 * 
	 * @return language
	 */
	public String getLanguage() {
		return this.language;
	}

	/**
	 * Getter method for "genre" property
	 * 
	 * @return genre
	 */
	public String getGenre() {
		return this.genre;
	}

	/**
	 * Getter method for "mood" property
	 * 
	 * @return mood
	 */
	public String getMood() {
		return this.mood;
	}

	/**
	 * Getter method for "album" property
	 * 
	 * @return album
	 */
	public String getAlbum() {
		return this.album;
	}

	/**
	 * Getter method for "year" property
	 * 
	 * @return year
	 */
	public String getYear() {
		return this.year;
	}

	/**
	 * Getter method for "albumArtist" property
	 * 
	 * @return albumArtist
	 */
	public String getAlbumArtist() {
		return this.albumArtist;
	}

	/**
	 * Getter method for "composer" property
	 * 
	 * @return composer
	 */
	public String getComposer() {
		return this.composer;
	}

	/**
	 * Getter method for "artist" property
	 * 
	 * @return artist
	 */
	public String getArtist() {
		return this.artist;
	}

	/**
	 * Getter method for "trackNbr" property
	 * 
	 * @return trackNbr
	 */
	public String getTrackNbr() {
		return this.trackNbr;
	}

	/**
	 * Getter method for "title" property
	 * 
	 * @return title
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * Getter method for "lyrics" property
	 * 
	 * @return lyrics
	 */
	public String getLyrics() {
		return this.lyrics;
	}

	/**
	 * Getter method for "tags" property
	 * 
	 * @return tags
	 */
	public String getTags() {
		return this.tags;
	}

	/**
	 * Getter method for "artwork" property
	 * 
	 * @return artwork
	 */
	public String getArtwork() {
		return this.artwork;
	}

	/**
	 * Getter method for "fileName" property
	 * 
	 * @return fileName
	 */
	public String getFileName() {
		return this.fileName;
	}

	/**
	 * Getter method for "filePath" property
	 * 
	 * @return filePath
	 */
	public String getFilePath() {
		return this.filePath;
	}

	/**
	 * Getter method for "tagErrors" property
	 * 
	 * @return tagErrors
	 */
	public List<String> getTagErrors() {
		return this.tagErrors;
	}

	/**
	 * class holding Mp3 header information
	 * 
	 * @version 1.0 Initial Version
	 * @author Rohit Narayanan
	 * @since Jan 13, 2011
	 */
	public class Header implements Serializable {
		/**
		 * serialVersionUID
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Size of the file
		 */
		private long size;

		/**
		 * Length of the trackNbr
		 */
		private int length;

		/**
		 * Encoding Mode
		 */
		private String mode;

		/**
		 * Encoding Bitrate Type
		 */
		private String bitrateType;

		/**
		 * Encoding Bitrate
		 */
		private String bitrate;

		/**
		 * Encoding Refquency
		 */
		private String frequency;

		/**
		 * @param aAudioHeader
		 * @param aFileSize
		 */
		public Header(AudioHeader aAudioHeader, long aFileSize) {
			this.size = aFileSize;
			this.length = aAudioHeader.getTrackLength();
			this.mode = aAudioHeader.getChannels();
			this.bitrateType = aAudioHeader.isVariableBitRate() ? "VBR" : "CBR";
			this.bitrate = aAudioHeader.getBitRate();
			this.frequency = aAudioHeader.getSampleRate();
		}

		/**
		 * Getter method for "size" property
		 * 
		 * @return size
		 */
		public long getSize() {
			return this.size;
		}

		/**
		 * Getter method for "length" property
		 * 
		 * @return length
		 */
		public int getLength() {
			return this.length;
		}

		/**
		 * Getter method for "mode" property
		 * 
		 * @return mode
		 */
		public String getMode() {
			return this.mode;
		}

		/**
		 * Getter method for "bitrateType" property
		 * 
		 * @return bitrateType
		 */
		public String getBitrateType() {
			return this.bitrateType;
		}

		/**
		 * Getter method for "bitrate" property
		 * 
		 * @return bitrate
		 */
		public String getBitrate() {
			return this.bitrate;
		}

		/**
		 * Getter method for "frequency" property
		 * 
		 * @return frequency
		 */
		public String getFrequency() {
			return this.frequency;
		}
	}

	/**
	 * static block to initialize metadata
	 */
	static {
		// standardFields.add(FieldKey.KEY);
		standardFields.add(FieldKey.LANGUAGE);
		standardFields.add(FieldKey.GENRE);
		// standardFields.add(FieldKey.MOOD);
		standardFields.add(FieldKey.ALBUM);
		standardFields.add(FieldKey.YEAR);
		standardFields.add(FieldKey.ALBUM_ARTIST);
		standardFields.add(FieldKey.COMPOSER);
		standardFields.add(FieldKey.ARTIST);
		standardFields.add(FieldKey.TITLE);
		standardFields.add(FieldKey.TRACK);
		// standardFields.add(FieldKey.LYRICS);
		// standardFields.add(FieldKey.TAGS);
		standardFields.add(FieldKey.COVER_ART);

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
}