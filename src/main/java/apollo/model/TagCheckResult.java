package apollo.model;

import accelerate.databean.AccelerateDataBean;

/**
 * Bean class to hold information on result of Mp3 Tag comparison
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since Jan 13, 2011
 */
public class TagCheckResult extends AccelerateDataBean {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -1L;

	/**
	 * Flag to indicate result of validation
	 */
	private boolean passed = false;

	/**
	 * Reason for result
	 */
	private String reason = null;

	/**
	 * Default Constructor
	 */
	public TagCheckResult() {
	}

	/**
	 * Overridden Constructor
	 * 
	 * @param aPassed
	 * @param aReason
	 */
	public TagCheckResult(boolean aPassed, String aReason) {
		this.passed = aPassed;
		this.reason = aReason;
	}

	/**
	 * Getter method for "passed" property
	 * 
	 * @return passed
	 */
	public boolean isPassed() {
		return this.passed;
	}

	/**
	 * Setter method for "passed" property
	 * 
	 * @param aPassed
	 */
	public void setPassed(boolean aPassed) {
		this.passed = aPassed;
	}

	/**
	 * Getter method for "reason" property
	 * 
	 * @return reason
	 */
	public String getReason() {
		return this.reason;
	}

	/**
	 * Setter method for "reason" property
	 * 
	 * @param aReason
	 */
	public void setReason(String aReason) {
		this.reason = aReason;
	}
}