package apollo.data.domain;

import java.util.Collection;

import org.springframework.data.annotation.Id;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.social.security.SocialUser;

/**
 * PUT DESCRIPTION HERE
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since Apr 9, 2016
 */
public class User extends SocialUser {
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
	 * @param aUsername
	 * @param aPassword
	 * @param aAuthorities
	 */
	public User(String aUsername, String aPassword, Collection<? extends GrantedAuthority> aAuthorities) {
		super(aUsername, aPassword, aAuthorities);
	}

}
