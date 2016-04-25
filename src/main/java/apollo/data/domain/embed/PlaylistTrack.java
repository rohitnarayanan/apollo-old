package apollo.data.domain.embed;

import java.io.Serializable;

/**
 * PUT DESCRIPTION HERE
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since Apr 9, 2016
 */
public class PlaylistTrack implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	private String trackId = null;

	/**
	 * 
	 */
	private long trackPosition = -1;

	/**
	 * Getter method for "trackId" property
	 * 
	 * @return trackId
	 */
	public String getTrackId() {
		return this.trackId;
	}

	/**
	 * Setter method for "trackId" property
	 * 
	 * @param aTrackId
	 */
	public void setTrackId(String aTrackId) {
		this.trackId = aTrackId;
	}

	/**
	 * Getter method for "trackPosition" property
	 * 
	 * @return trackPosition
	 */
	public long getTrackPosition() {
		return this.trackPosition;
	}

	/**
	 * Setter method for "trackPosition" property
	 * 
	 * @param aTrackPosition
	 */
	public void setTrackPosition(long aTrackPosition) {
		this.trackPosition = aTrackPosition;
	}
}
