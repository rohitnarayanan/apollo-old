package apollo.data.domain;

import java.util.List;

import org.springframework.data.annotation.Id;

/**
 * PUT DESCRIPTION HERE
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since Apr 9, 2016
 */
public class User extends org.springframework.security.core.userdetails.User {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	@Id
	private String id = null;

	/**
	 * 
	 */
	private List<String> roles = null;

	/**
	 * 
	 */
	private String name = null;

	/**
	 * 
	 */
	private String email = null;

	/**
	 * @param aUsername
	 * @param aPassword
	 */
	public User(String aUsername, String aPassword) {
		super(aUsername, aPassword, null);
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
	 * Setter method for "id" property
	 * 
	 * @param aId
	 */
	public void setId(String aId) {
		this.id = aId;
	}

	/**
	 * Getter method for "roles" property
	 * 
	 * @return roles
	 */
	public List<String> getRoles() {
		return this.roles;
	}

	/**
	 * Setter method for "roles" property
	 * 
	 * @param aRoles
	 */
	public void setRoles(List<String> aRoles) {
		this.roles = aRoles;
	}

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
	 * Getter method for "email" property
	 * 
	 * @return email
	 */
	public String getEmail() {
		return this.email;
	}

	/**
	 * Setter method for "email" property
	 * 
	 * @param aEmail
	 */
	public void setEmail(String aEmail) {
		this.email = aEmail;
	}
}
