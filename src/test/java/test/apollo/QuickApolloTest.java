package test.apollo;

import java.io.File;

import apollo.util.ID3Util;

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
			File file = new File(
					"C:\\Temp\\M\\Library\\Hindi\\TempGenre\\TempArtist\\TempAlbum\\Hindi-TempGenre-TempAlbum-2016-TempArtist-TempTitle_1.zzz");
			System.out.println(ID3Util.tempTag(file));
		} catch (Exception error) {
			error.printStackTrace();
		}
	}
}
