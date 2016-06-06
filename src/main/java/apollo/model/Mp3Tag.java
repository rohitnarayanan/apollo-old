package apollo.model;

import java.io.File;
import java.io.Serializable;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import accelerate.databean.AccelerateDataBean;

/**
 * Model class holding Mp3 tag information
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since December 13, 2010
 */
public class Mp3Tag extends AccelerateDataBean {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

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
	public class Artwork extends AccelerateDataBean {
		/**
		 * serialVersionUID
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Size of the file
		 */
		public org.jaudiotagger.tag.images.Artwork jatArtwork = null;

		/**
		 * Length of the trackNbr
		 */
		public String base64Data = null;

		/**
		* 
		*/
		public void render() {
			if (this.jatArtwork != null) {
				Base64.Encoder encoder = Base64.getEncoder();
				this.base64Data = encoder.encodeToString(this.jatArtwork.getBinaryData());
			}
		}
	}

	/**
	 * {@link Header} information
	 */
	public Header header = new Header();

	/**
	 * Language
	 */
	public String language = null;

	/**
	 * Genre
	 */
	public String genre = null;

	/**
	 * Album
	 */
	public String album = null;

	/**
	 * Album Year
	 */
	public String year = null;

	/**
	 * Album Composer
	 */
	public String composer = null;

	/**
	 * Album Artist
	 */
	public String albumArtist = null;

	/**
	 * PlaylistTrack Artist
	 */
	public String artist = null;

	/**
	 * PlaylistTrack Number
	 */
	public String trackNbr = null;

	/**
	 * PlaylistTrack Title
	 */
	public String title = null;

	/**
	 * Album Artwork
	 */
	public Artwork artwork = new Artwork();

	/**
	 * Album Artwork File
	 */
	@JsonIgnore
	public File artworkFile = null;

	/**
	 * {@link File} instance
	 */
	public File sourceFile = null;

	/**
	 * @param aMp3Tag
	 */
	public void copyFrom(Mp3Tag aMp3Tag) {
		this.language = StringUtils.defaultString(aMp3Tag.language, this.language);
		this.genre = StringUtils.defaultString(aMp3Tag.genre, this.genre);
		this.album = StringUtils.defaultString(aMp3Tag.album, this.album);
		this.year = StringUtils.defaultString(aMp3Tag.year, this.year);
		this.composer = StringUtils.defaultString(aMp3Tag.composer, this.composer);
		this.albumArtist = StringUtils.defaultString(aMp3Tag.albumArtist, this.albumArtist);
		this.artist = StringUtils.defaultString(aMp3Tag.artist, this.artist);
		this.trackNbr = StringUtils.defaultString(aMp3Tag.trackNbr, this.trackNbr);
		this.title = StringUtils.defaultString(aMp3Tag.title, this.title);
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
	 * Setter method for "language" property
	 * 
	 * @param aLanguage
	 */
	public void setLanguage(String aLanguage) {
		this.language = aLanguage;
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
	 * Setter method for "genre" property
	 * 
	 * @param aGenre
	 */
	public void setGenre(String aGenre) {
		this.genre = aGenre;
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
	 * Setter method for "album" property
	 * 
	 * @param aAlbum
	 */
	public void setAlbum(String aAlbum) {
		this.album = aAlbum;
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
	 * Setter method for "year" property
	 * 
	 * @param aYear
	 */
	public void setYear(String aYear) {
		this.year = aYear;
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
	 * Setter method for "composer" property
	 * 
	 * @param aComposer
	 */
	public void setComposer(String aComposer) {
		this.composer = aComposer;
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
	 * Setter method for "albumArtist" property
	 * 
	 * @param aAlbumArtist
	 */
	public void setAlbumArtist(String aAlbumArtist) {
		this.albumArtist = aAlbumArtist;
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
	 * Setter method for "artist" property
	 * 
	 * @param aArtist
	 */
	public void setArtist(String aArtist) {
		this.artist = aArtist;
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
	 * Setter method for "trackNbr" property
	 * 
	 * @param aTrackNbr
	 */
	public void setTrackNbr(String aTrackNbr) {
		this.trackNbr = aTrackNbr;
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
	 * Setter method for "title" property
	 * 
	 * @param aTitle
	 */
	public void setTitle(String aTitle) {
		this.title = aTitle;
	}

	/**
	 * Getter method for "artworkFile" property
	 * 
	 * @return artworkFile
	 */
	public File getArtworkFile() {
		return this.artworkFile;
	}

	/**
	 * Setter method for "artworkFile" property
	 * 
	 * @param aArtworkFile
	 */
	public void setArtworkFile(File aArtworkFile) {
		this.artworkFile = aArtworkFile;
	}

	/**
	 * Getter method for "sourceFile" property
	 * 
	 * @return sourceFile
	 */
	public File getSourceFile() {
		return this.sourceFile;
	}

	/**
	 * Setter method for "sourceFile" property
	 * 
	 * @param aSourceFile
	 */
	public void setSourceFile(File aSourceFile) {
		this.sourceFile = aSourceFile;
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
	 * Getter method for "artwork" property
	 * 
	 * @return artwork
	 */
	public Artwork getArtwork() {
		return this.artwork;
	}
}