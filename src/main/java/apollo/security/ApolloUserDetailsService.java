package apollo.security;

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
public class ApolloUserDetailsService implements UserDetailsService {
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
		User user = this.userRepository.findByUsername(aUsername);

		if (user == null) {
			throw new UsernameNotFoundException("Unknown User: " + aUsername);
		}

		ApolloSession apolloSession = new ApolloSession(user);
		return apolloSession;
	}
}