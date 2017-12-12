package apollo;

import org.springframework.boot.SpringApplication;

import apollo.config.ApolloConfig;

/**
 * Main class for this Boot Application
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since December 11, 2017
 */
public class ApolloBootApplication {
	/**
	 * Main method to start web context as spring boot application
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(ApolloConfig.class, args);
	}
}
