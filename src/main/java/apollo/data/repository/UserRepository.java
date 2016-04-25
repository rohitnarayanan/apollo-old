package apollo.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import apollo.data.domain.User;

/**
 * PUT DESCRIPTION HERE
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since Apr 9, 2016
 */
public interface UserRepository extends MongoRepository<User, String> {
	/**
	 * @param aUsername
	 * @return
	 */
	public User findByUsername(String aUsername);
}