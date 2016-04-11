package apollo.data.repository;

import java.util.List;

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

	/**
	 * @param firstName
	 * @return
	 */
	public User findByFirstName(String firstName);

	/**
	 * @param lastName
	 * @return
	 */
	public List<User> findByLastName(String lastName);

}