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
		/*
		 * TODO: Uncomment the following code when backend is ready.
		 */
		// User user = this.userRepository.findByUsername(aUsername);
		// if (user == null) {
		// throw new UsernameNotFoundException("Unknown User: " + aUsername);
		// }

		/*
		 * TODO: Remove the default user and throw appropriate exception to
		 * display error on login page.
		 */
		User user = new User();
		user.setUsername("q");
		user.setPassword("q");
		user.setId("1");
		user.setEmail("rohit.nn@gmail.com");
		user.setRoles(Arrays.asList("user"));

		ApolloSession apolloSession = new ApolloSession(user);
		return apolloSession;
	}
}