package apollo.security;

import java.util.Arrays;

import org.slf4j.Logger;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import accelerate.utils.logging.AutowireLogger;
import apollo.data.domain.User;

/**
 * PUT DESCRIPTION HERE
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since Apr 15, 2016
 */
@Component
public class ApolloUserDetailsService implements UserDetailsService {
	/**
	 * 
	 */
	@AutowireLogger
	private Logger _logger = null;

	/**
	 * 
	 */
	// @Autowired
	// private UserRepository userRepository = null;

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
		User user = new User();
		user.setId("1");
		user.setRoles(Arrays.asList("user"));
		user.setName("Rohit");
		user.setEmail("rohit.nn@gmail.com");

		ApolloSession apolloSession = new ApolloSession(user);
		apolloSession.setUsername("q");
		apolloSession.setPassword("q");
		return apolloSession;
	}
}