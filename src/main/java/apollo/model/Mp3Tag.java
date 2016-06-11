package apollo.model;

import static accelerate.util.AppUtil.compare;

import java.io.File;
import java.io.Serializable;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.tag.images.StandardArtwork;

import accelerate.databean.AccelerateDataBean;

/**
 * Model class holding Mp3 tag information
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since December 13, 2010
 */
public class Mp3Tag extends AccelerateDataBean implements Comparable<Mp3Tag> {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * {@link Header} information
	 */
	public Header header = new Header();

	/**
	 * Id
	 */
	public String id = null;

	/**
	 * Language
	 */
	public String language = null;

	/**
	 * Genre
	 */
	public String genre = null;

	/**
	 * Mood
	 */
	public String mood = null;

	/**
	 * Album
	 */
	public String album = null;

	/**
	 * Album Year
	 */
	public String year = null;

	/**
	 * Album Artist
	 */
	public String albumArtist = null;

	/**
	 * Album Composer
	 */
	public String composer = null;

	/**
	 * Track Artist
	 */
	public String artist = null;

	/**
	 * Track Number
	 */
	public String trackNbr = null;

	/**
	 * Track Title
	 */
	public String title = null;

	/**
	 * Track Lyrics
	 */
	public String lyrics = null;

	/**
	 * Tags
	 */
	public String tags = null;

	/**
	 * Album Artwork
	 */
	public Artwork artwork = new Artwork();

	/**
	 * Name of the file
	 */
	public String fileName = null;

	/**
	 * {@link File} instance
	 */
	public File sourceFile = null;

	/**
	 * {@link TagCheckResult} instance
	 */
	public TagCheckResult tagCheckResult = null;

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
		public long size = 0;

		/**
		 * Length of the trackNbr
		 */
		public int length = 0;

		/**
		 * Encoding Mode
		 */
		public String mode = null;

		/**
		 * Encoding Bitrate Type
		 */
		public String bitrateType = null;

		/**
		 * Encoding Bitrate
		 */
		public String bitrate = null;

		/**
		 * Encoding Refquency
		 */
		public String frequency = null;
	}

	/**
	 * class holding Mp3 header information
	 * 
	 * @version 1.0 Initial Version
	 * @author Rohit Narayanan
	 * @since Jan 13, 2011
	 */
	public class Artwork implements Serializable, Comparable<Artwork> {
		/**
		 * serialVersionUID
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Length of the trackNbr
		 */
		public String base64Data = null;

		/**
		 * Album Artwork File
		 */
		public File artworkFile = null;

		/**
		 * Method to encode the artwork image to Base64 string
		 *
		 * @param aJATArtwork
		 *            instance of org.jaudiotagger.tag.images.Artwork
		 */
		public void encode(org.jaudiotagger.tag.images.Artwork aJATArtwork) {
			if (aJATArtwork != null) {
				Base64.Encoder encoder = Base64.getEncoder();
				this.base64Data = encoder.encodeToString(aJATArtwork.getBinaryData());
			}
		}

		/**
		 * Method to decode the artwork image from Base64 string
		 *
		 * @return instance of org.jaudiotagger.tag.images.Artwork
		 */
		public org.jaudiotagger.tag.images.Artwork decode() {
			org.jaudiotagger.tag.images.Artwork jatArtwork = null;

			if (this.base64Data != null) {
				Base64.Decoder decoder = Base64.getDecoder();
				jatArtwork = new StandardArtwork();
				jatArtwork.setBinaryData(decoder.decode(this.base64Data));
			}

			return jatArtwork;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		/**
		 * @param aArtwork
		 * @return
		 */
		@Override
		public int compareTo(Artwork aArtwork) {
			if (!compare(this.base64Data, aArtwork.base64Data)) {
				return -1;
			}

			return 0;
		}
	}

	/**
	 * default constructor
	 */
	public Mp3Tag() {
		addJsonIgnoreFields("header", "artwork", "tagCheckResult");
	}

	/**
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
		this.artwork = aMp3Tag.artwork;
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

}