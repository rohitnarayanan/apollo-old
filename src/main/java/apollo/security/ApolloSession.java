package apollo.security;

import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import accelerate.web.security.AccelerateUserSession;
import apollo.data.domain.User;

/**
 * Extension of {@link AccelerateUserSession} to add custom session variables
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since Apr 15, 2016
 */
public class ApolloSession extends AccelerateUserSession {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * {@link User} instance
	 */
	private User user = null;

	/**
	 * @param aUser
	 */
	public ApolloSession(User aUser) {
		this.user = aUser;
		setAuthorities(
				aUser.getRoles().stream().map(aRole -> new SimpleGrantedAuthority(aRole)).collect(Collectors.toList()));
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
