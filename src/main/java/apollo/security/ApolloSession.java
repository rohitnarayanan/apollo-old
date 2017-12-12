package apollo.security;

import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import accelerate.web.Session;
import accelerate.web.security.AccelerateUserSession;
import apollo.data.domain.User;

/**
 * PUT DESCRIPTION HERE
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since Apr 15, 2016
 */
public class ApolloSession  {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	private User user = null;

	/**
	 * default constructor
	 */
	public ApolloSession() {
	}

	/**
	 * copy constructor
	 * 
	 * @param aUser
	 */
	public ApolloSession(User aUser) {
		super(aUser.getUsername());
		setPassword(aUser.getPassword());
		setAuthorities(
				aUser.getRoles().stream().map(aRole -> new SimpleGrantedAuthority(aRole)).collect(Collectors.toList()));

		this.user = aUser;
	}

	/**
	 * Getter method for "user" property
	 * 
	 * @return user
	 */
	public User getUser() {
		return this.user;
	}

	/**
	 * Setter method for "user" property
	 * 
	 * @param aUser
	 */
	public void setUser(User aUser) {
		this.user = aUser;
	}
}
