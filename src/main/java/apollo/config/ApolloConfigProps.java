package apollo.config;

import java.io.Serializable;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * PUT DESCRIPTION HERE
 * 
 * @version 1.0 Initial Version
 * @author TCS
 * @since Feb 4, 2016
 */
@Component
@ConfigurationProperties(prefix = "apollo.configProps")
public class ApolloConfigProps implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	private String fileExtn = null;

	/**
	 *
	 */
	private String libraryRoot = null;

	/**
	 *
	 */
	private String fileSelectorRoot = null;

	/**
	 * Getter method for "fileExtn" property
	 * 
	 * @return fileExtn
	 */
	public String getFileExtn() {
		return this.fileExtn;
	}

	/**
	 * Setter method for "fileExtn" property
	 * 
	 * @param aFileExtn
	 */
	public void setFileExtn(String aFileExtn) {
		this.fileExtn = aFileExtn;
	}

	/**
	 * Getter method for "libraryRoot" property
	 * 
	 * @return libraryRoot
	 */
	public String getLibraryRoot() {
		return this.libraryRoot;
	}

	/**
	 * Setter method for "libraryRoot" property
	 * 
	 * @param aLibraryRoot
	 */
	public void setLibraryRoot(String aLibraryRoot) {
		this.libraryRoot = aLibraryRoot;
	}

	/**
	 * Getter method for "fileSelectorRoot" property
	 * 
	 * @return fileSelectorRoot
	 */
	public String getFileSelectorRoot() {
		return this.fileSelectorRoot;
	}

	/**
	 * Setter method for "fileSelectorRoot" property
	 * 
	 * @param aFileSelectorRoot
	 */
	public void setFileSelectorRoot(String aFileSelectorRoot) {
		this.fileSelectorRoot = aFileSelectorRoot;
	}
}
