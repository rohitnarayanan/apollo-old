package test.apollo;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;

import accelerate.exception.AccelerateException;
import accelerate.util.AppUtil;
import accelerate.util.FileUtil;
import accelerate.util.JSONUtil;
import accelerate.util.StringUtil;
import apollo.model.Mp3Tag;
import apollo.util.ApolloConstants;
import apollo.util.Mp3TagUtil;

@SuppressWarnings("all")
public class QuickTagHelper {
	static File root = new File("/Users/rohitnarayanan/Music/Unorganized/Parikrama");

	static List<String> parseTagTokens = ID3Util.parseTagExpression("<trackNbr> - ABCD2 - <title> [Songspk.LINK]");
	static Mp3Tag commonTag = null;
	static List<FieldKey> fieldKeys = new ArrayList<>();
	static boolean testRun = false;

	static {
		fieldKeys.add(FieldKey.valueOf("LANGUAGE"));
		fieldKeys.add(FieldKey.valueOf("GENRE"));
		fieldKeys.add(FieldKey.valueOf("ALBUM"));
		fieldKeys.add(FieldKey.valueOf("COMPOSER"));
		fieldKeys.add(FieldKey.valueOf("ALBUM_ARTIST"));
		fieldKeys.add(FieldKey.valueOf("ARTIST"));
		fieldKeys.add(FieldKey.valueOf("TITLE"));
		fieldKeys.add(FieldKey.valueOf("TRACK"));
		fieldKeys.add(FieldKey.valueOf("YEAR"));
	}

	public static void main(String[] args) {
		try {
			QuickTagHelper tagManager = new QuickTagHelper();
			DirectoryParser.execute(root, tagManager);

			if (commonTag != null) {
				System.err.println(commonTag.toLog());
			}
		} catch (Exception error) {
			error.printStackTrace();
		}

		try {
			// testRun = true;

			// removeTag(aFile);
			writeTag(aFile);
			// aFile = renameMp3(aFile);
			readTag(aFile);

			return aFile;
		} catch (Exception error) {
			System.err.println(aFile);
			// System.err.println(error.getMessage());
			error.printStackTrace();
		}
	}

	public void writeTag(File aFile) throws AccelerateException {
		String albumArtist = null;
		Mp3Tag mp3Tag = new Mp3Tag();

		mp3Tag.setField(Mp3Tag.LANGUAGE, FileUtil.getParent(aFile, 4).getName());
		mp3Tag.setField(Mp3Tag.GENRE, FileUtil.getParent(aFile, 3).getName());
		mp3Tag.setField(Mp3Tag.ALBUM_ARTIST, FileUtil.getParent(aFile, 2).getName());
		mp3Tag.setField(Mp3Tag.ALBUM, FileUtil.getParent(aFile, 1).getName());
		mp3Tag.setField(Mp3Tag.TITLE, FileUtil.getFileName(aFile));

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

		Path artworkPath = aFile.getParentFile().toPath().resolve("folder.jpg");

		if (Files.exists(artworkPath)) {
			mp3Tag.setField(mp3Tag.ARTWORK, "X," + Base64.getEncoder().encodeToString(Files.readAllBytes(artworkPath)));
		}

		System.out.println(mp3Tag);

		if (!testRun) {
			mp3Tag.save();
		}
	}

	public File renameMp3(File aFile) throws AccelerateException {
		Mp3Tag mp3Tag = new Mp3Tag(aFile);
		String title = mp3Tag.getTitle();

		if (CommonUtils.isEmpty(title)) {
			System.err.println("Tag not set:" + aFile);
			System.err.println(mp3Tag.toLog());
			return aFile;
		} else {
			title = StringUtil.applyPatternReplace(title, "/", "-", true);
		}

		if (FileUtil.getFileName(aFile).equals(title)) {
			return aFile;
		}

		File newFile = aFile;
		if (!testRun) {
			newFile = FileUtil.renameFile(aFile, title);
			System.out.println(newFile);
		} else {
			System.out.println(FileUtil.getFileName(aFile) + "##" + title);
		}
		
		Paths.get(null).

		return newFile;
	}

	public void removeTag(File aTrack) throws AccelerateException {
		MP3File mp3File = Mp3TagUtil.getMP3File(aTrack);
		if (mp3File.hasID3v1Tag()) {
			Mp3TagUtil.deleteV1Tag(aTrack);
			System.out.println("Deleting V1 :" + new Mp3Tag(aTrack));
		} else {
			// ID3Util.deleteTag(aFile);
			// System.out.println("Deleting V2 :" + mp3Tag.toLog());
		}
	}
}
