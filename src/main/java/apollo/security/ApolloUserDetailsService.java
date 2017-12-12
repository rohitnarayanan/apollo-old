package apollo.security;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import apollo.data.domain.User;
import apollo.data.repository.UserRepository;

/**
 * PUT DESCRIPTION HERE
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since Apr 15, 2016
 */
@Component
@SuppressWarnings("unused")
public class ApolloUserDetailsService implements UserDetailsService {
	/**
	 * 
	 */
	private static Logger LOGGER = LoggerFactory.getLogger(ApolloUserDetailsService.class);

	/**
	 * 
	 */
	@Autowired
	private UserRepository userRepository = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.core.userdetails.UserDetailsService#
	 * loadUserByUsername(java.lang.String)
	 */
	/**
	 * @param aUsername
	 * @return
	 * @throws UsernameNotFoundException
	 */
	@Override
	public UserDetails loadUserByUsername(String aUsername) throws UsernameNotFoundException {
		User user = new User("q", "q");
		user.setId("1");
		user.setRoles(Arrays.asList("user"));
		user.setName("Rohit");
		user.setEmail("rohit.nn@gmail.com");

		return user;
	}
}