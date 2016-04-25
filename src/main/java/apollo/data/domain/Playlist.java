package apollo.data.domain;

import java.util.List;

import apollo.data.domain.embed.PlaylistTrack;
import apollo.data.type.PlaylistType;

/**
 * PUT DESCRIPTION HERE
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since Apr 9, 2016
 */
public class Playlist extends AbstractDomain {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	private String name = null;

	/**
	 * 
	 */
	private PlaylistType type = null;

	/**
	 * 
	 */
	private List<String> tags = null;

	/**
	 * 
	 */
	private long trackLimit = -1;

	/**
	 * 
	 */
	private List<PlaylistTrack> tracks = null;

	/**
	 * Getter method for "name" property
	 * 
	 * @return name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Setter method for "name" property
	 * 
	 * @param aName
	 */
	public void setName(String aName) {
		this.name = aName;
	}

	/**
	 * Getter method for "type" property
	 * 
	 * @return type
	 */
	public PlaylistType getType() {
		return this.type;
	}

	/**
	 * Setter method for "type" property
	 * 
	 * @param aType
	 */
	public void setType(PlaylistType aType) {
		this.type = aType;
	}

	/**
	 * Getter method for "tags" property
	 * 
	 * @return tags
	 */
	public List<String> getTags() {
		return this.tags;
	}

	/**
	 * Setter method for "tags" property
	 * 
	 * @param aTags
	 */
	public void setTags(List<String> aTags) {
		this.tags = aTags;
	}

	/**
	 * Getter method for "trackLimit" property
	 * 
	 * @return trackLimit
	 */
	public long getTrackLimit() {
		return this.trackLimit;
	}

	/**
	 * Setter method for "trackLimit" property
	 * 
	 * @param aTrackLimit
	 */
	public void setTrackLimit(long aTrackLimit) {
		this.trackLimit = aTrackLimit;
	}

	/**
	 * Getter method for "tracks" property
	 * 
	 * @return tracks
	 */
	public List<PlaylistTrack> getTracks() {
		return this.tracks;
	}

	/**
	 * Setter method for "tracks" property
	 * 
	 * @param aTracks
	 */
	public void setTracks(List<PlaylistTrack> aTracks) {
		this.tracks = aTracks;
	}
}
