package test.apollo;

import java.io.File;
import java.util.List;

import apollo.model.Mp3Tag;
import apollo.util.Mp3TagUtil;

/**
 * Junit test for accelerate spring context
 *
 * @author Rohit Narayanan
 * @version 1.0 Initial Version
 * @since Jul 20, 2014
 */
public class QuickApolloTest {
	/**
	 * Main method to start web context as spring boot application
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			// File file = new File();
			String tokens = "<artist> - <title>";
			List<String> parseTokens = Mp3TagUtil.parseTagExpression(tokens);
			Mp3Tag songTag = new Mp3Tag(
					new File("/Users/rohitnarayanan/Music/Unorganized/Assorted Indi Pop/Jal - Kash Yeh Pal.mp3"),
					parseTokens);
			System.out.println(songTag);
		} catch (Exception error) {
			error.printStackTrace();
		}
	}
}
