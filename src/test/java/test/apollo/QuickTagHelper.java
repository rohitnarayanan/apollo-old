package test.apollo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import org.jaudiotagger.audio.mp3.MP3File;

import accelerate.utils.NIOUtil;
import accelerate.utils.exception.AccelerateException;
import apollo.model.Mp3Tag;
import apollo.util.Mp3TagUtil;

@SuppressWarnings("all")
public class QuickTagHelper {
	static boolean testRun = true;

	public static void main(String[] args) {
		try {
			Path songPath = Paths.get("C:\\Users\\185710\\Downloads\\185710.pdf");
			writeTag(songPath);

		} catch (Exception error) {
			error.printStackTrace();
		}

		// try {
		// testRun = true;

		// removeTag(aFile);
		// writeTag(aFile);
		// aFile = renameMp3(aFile);
		// readTag(aFile);

		// return aFile;
		// } catch (Exception error) {
		// System.err.println(aFile);
		// System.err.println(error.getMessage());
		// error.printStackTrace();
		// }
	}

	private static void writeTag(Path aSongPath) throws IOException {
		String albumArtist = null;
		Mp3Tag mp3Tag = new Mp3Tag();

		// mp3Tag.setField(Mp3Tag.LANGUAGE,
		// NIOUtil.getFileName(NIOUtil.getParent(songPath, 4)));
		mp3Tag.setField(Mp3Tag.GENRE, NIOUtil.getBaseName(NIOUtil.getParent(aSongPath, 3)));
		mp3Tag.setField(Mp3Tag.ALBUM_ARTIST, NIOUtil.getBaseName(NIOUtil.getParent(aSongPath, 2)));
		mp3Tag.setField(Mp3Tag.ALBUM, NIOUtil.getBaseName(NIOUtil.getParent(aSongPath, 1)));
		mp3Tag.setField(Mp3Tag.TITLE, NIOUtil.getBaseName(aSongPath));

		albumArtist = "";

		mp3Tag.setField(Mp3Tag.LANGUAGE, "English");
		mp3Tag.setField(Mp3Tag.GENRE, "Rock");
		mp3Tag.setField(Mp3Tag.MOOD, "");
		mp3Tag.setField(Mp3Tag.ALBUM_ARTIST, albumArtist);
		mp3Tag.setField(Mp3Tag.ALBUM, "Parikrama");
		mp3Tag.setField(Mp3Tag.YEAR, "2014");
		mp3Tag.setField(Mp3Tag.COMPOSER, albumArtist);
		mp3Tag.setField(Mp3Tag.ARTIST, albumArtist);
		mp3Tag.setField(Mp3Tag.TRACK_NBR, "");
		mp3Tag.setField(Mp3Tag.TITLE, "Naina");
		mp3Tag.setField(Mp3Tag.LYRICS, "");
		mp3Tag.setField(Mp3Tag.TAGS, "");

		Path artworkPath = aSongPath.getParent().resolve("folder.jpg");

		if (Files.exists(artworkPath)) {
			mp3Tag.setField(mp3Tag.ARTWORK, "X," + Base64.getEncoder().encodeToString(Files.readAllBytes(artworkPath)));
		}

		System.out.println(mp3Tag);

		// testRun = false;
		if (!testRun) {
			mp3Tag.save();
		}
	}

	public void removeTag(Path aSongPath) throws AccelerateException {
		MP3File mp3File = Mp3TagUtil.getMP3File(aSongPath);
		Mp3Tag mp3Tag = Mp3TagUtil.deleteV1Tag(aSongPath);
		System.out.println("V1 Tag Deleted:" + mp3Tag);
	}
}
