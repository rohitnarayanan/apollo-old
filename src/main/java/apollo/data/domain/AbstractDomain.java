package apollo.data.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * PUT DESCRIPTION HERE
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since Apr 9, 2016
 */
public class AbstractDomain implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	private String createdBy = null;

	/**
	 * 
	 */
	private Date createdAt = null;

	/**
	 * 
	 */
	private String updatedBy = null;

	/**
	 * 
	 */
	private Date updatedAt = null;

	/**
	 * Getter method for "createdBy" property
	 * 
	 * @return createdBy
	 */
	public String getCreatedBy() {
		return this.createdBy;
	}

	/**
	 * Setter method for "createdBy" property
	 * 
	 * @param aCreatedBy
	 */
	public void setCreatedBy(String aCreatedBy) {
		this.createdBy = aCreatedBy;
	}

	/**
	 * Getter method for "createdAt" property
	 * 
	 * @return createdAt
	 */
	public Date getCreatedAt() {
		return this.createdAt;
	}

	/**
	 * Setter method for "createdAt" property
	 * 
	 * @param aCreatedAt
	 */
	public void setCreatedAt(Date aCreatedAt) {
		this.createdAt = aCreatedAt;
	}

	/**
	 * Getter method for "updatedBy" property
	 * 
	 * @return updatedBy
	 */
	public String getUpdatedBy() {
		return this.updatedBy;
	}

	/**
	 * Setter method for "updatedBy" property
	 * 
	 * @param aUpdatedBy
	 */
	public void setUpdatedBy(String aUpdatedBy) {
		this.updatedBy = aUpdatedBy;
	}

	/**
	 * Getter method for "updatedAt" property
	 * 
	 * @return updatedAt
	 */
	public Date getUpdatedAt() {
		return this.updatedAt;
	}

	/**
	 * Setter method for "updatedAt" property
	 * 
	 * @param aUpdatedAt
	 */
	public void setUpdatedAt(Date aUpdatedAt) {
		this.updatedAt = aUpdatedAt;
	}
}
