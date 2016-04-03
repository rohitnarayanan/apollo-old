package test.apollo;

import org.springframework.boot.SpringApplication;

import apollo.config.ApolloConfig;

/**
 * Junit test for accelerate spring context
 *
 * @author Rohit Narayanan
 * @version 1.0 Initial Version
 * @since Jul 20, 2014
 */
public class ApolloTest {
	/**
	 * Main method to start web context as spring boot application
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(ApolloConfig.class, args);
	}
}
