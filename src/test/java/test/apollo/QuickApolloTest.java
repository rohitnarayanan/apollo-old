package test.apollo;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

import accelerate.util.AppUtil;
import accelerate.util.FileUtil;

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
			final String aFileType = "none";
			File[] fileList = new File("C:\\Sites\\todo").listFiles(new FileFilter() {
				@Override
				public boolean accept(File aInnerFile) {
					boolean flag = aInnerFile.isDirectory();
					System.out.println(aInnerFile + "<1>" + flag);
					flag = AppUtil.compare(aFileType, "none") ? false
							: AppUtil.compare(aFileType, FileUtil.getFileExtn(aInnerFile));
					System.out.println(aInnerFile + "<2>" + flag);
					flag = aInnerFile.isDirectory() || (AppUtil.compare(aFileType, "none") ? false
							: AppUtil.compare(aFileType, FileUtil.getFileExtn(aInnerFile)));
					System.out.println(aInnerFile + "<3>" + flag);
					return flag;
				}
			});
			Arrays.stream(fileList).forEach(aFile -> System.out.println(aFile));
		} catch (Exception error) {
			error.printStackTrace();
		}
	}
}
