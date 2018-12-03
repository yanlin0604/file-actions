package com.file_io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

/**
 * 文件操作工具类
 * File Manipulation Tool Class
 * @author Juanjuan
 */
public class FileUtils {

	private static final char UNIX_SEPARATOR = '/';

	private static final char WINDOWS_SEPARATOR = '\\';

	private static final char EXTENSION_SEPARATOR = '.';

	/**
	 * Returns the index of the last extension separator character, which is a dot. 返回最后"."的位置
	 * <p>
	 * This method also checks that there is no directory separator after the last dot. To do this it uses
	 * {@link #indexOfLastSeparator(String)} which will handle a file in either Unix or Windows format.
	 * <p>
	 * The output will be the same irrespective of the machine that the code is running on.
	 * 
	 * @param filename
	 *            the filename to find the last path separator in, null returns -1
	 * @return the index of the last separator character, or -1 if there is no such character
	 */
	public static int indexOfExtension(String filename) {
		if (filename == null) {
			return -1;
		}
		int extensionPos = filename.lastIndexOf(EXTENSION_SEPARATOR);
		int lastSeparator = indexOfLastSeparator(filename);
		return (lastSeparator > extensionPos ? -1 : extensionPos);
	}

	/**
	 * Returns the index of the last directory separator character. 返回最后目录分割符的位置(unix or window)
	 * <p>
	 * This method will handle a file in either Unix or Windows format. The position of the last forward or backslash is
	 * returned.
	 * <p>
	 * The output will be the same irrespective of the machine that the code is running on.
	 * 
	 * @param filename
	 *            the filename to find the last path separator in, null returns -1
	 * @return the index of the last separator character, or -1 if there is no such character
	 */
	public static int indexOfLastSeparator(String filename) {
		if (filename == null) {
			return -1;
		}
		int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
		int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
		return Math.max(lastUnixPos, lastWindowsPos);
	}

	/**
	 * 从全文件名字符串中取得最后目录分隔符后的字串返回，支持unix和windows文件分隔符
	 * String return after obtaining the last directory separator from the full filename string, supporting UNIX and Windows file separators
	 * <pre>
	 * example:
	 *   "D:/test/mydoc.txt"      --> "<strong>mydoc.txt</strong>"
	 *   "\\usr\\conf\\srun.tar"  --> "<strong>srun.tar</strong>"
	 *   "\\tmp\\tss\\c"          --> "<strong>c</strong>"
	 * </pre>
	 * 
	 * @param fullFileName
	 *            抽象文件名字串,若为null则方法返回null.
	 *            Abstract file name string, if NULL, the method returns NULL.
	 * @return String
	 */
	public static String getFilename(final String fullFilename) {
		if (fullFilename == null) {
			return null;
		}
		final int index = indexOfLastSeparator(fullFilename);
		return fullFilename.substring(index + 1);

	}

	/**
	 * Gets the extension of a filename 获取文件扩展名，即截取字串中最后一个点“。”号(不包括点号)后面的字串作为扩展名返回.
	 * <p>
	 * This method returns the textual part of the filename after the last dot. There must be no directory separator
	 * after the dot.
	 * 
	 * <pre>
	 * example:
	 * 
	 *   D:/d.new.txt      --> "txt"
	 *   a/b/c.jpg         --> "jpg"
	 *   a/b.txt/c         --> ""
	 *   a/b/c             --> ""
	 * </pre>
	 * <p>
	 * The output will be the same irrespective of the machine that the code is running on.
	 * 
	 * @param filename
	 *            the filename to retrieve the extension of.
	 * @return the extension of the file or an empty string if none exists or <code>null</code> if the filename is
	 *         <code>null</code>.
	 */
	public static String getFilenameExtension(final String filename) {

		if (filename == null) {
			return null;
		}
		int index = indexOfExtension(filename);
		if (index == -1) {
			return "";
		}
		else {
			return filename.substring(index + 1);
		}
	}

	/**
	 * 剥离抽象文件名称的文件扩展名,即截取最后"."号前的字串
	 * Peel the file name extension of the abstract file name, which is to intercept the string before the last  ". " Sign
	 * <pre>
	 * example:
	 *    "D:/test/mydoc.txt"    --> "D:/test/mydoc"
	 *    "aaaa.bbb.ccc"         --> "aaaa.bbb"
	 * </pre>
	 * 
	 * @param filename
	 *            待处理的文件名称字串
	 *             File name string to be processed
	 * @return
	 */
	public static String stripFilenameExtension(final String filename) {
		int index = indexOfExtension(filename);
		return (index == -1 ? filename : filename.substring(0, index));
	}

	/**
	 * 创建文件目录(包括子目录) 支持创建多级文件目录，如“d:/aaa/bbb/ccc”
	 * Creating a file directory, including subdirectory records, supports the creation of multilevel file directories, such as "D:/AAA/BBB/CCC"
	 * @param directory
	 *            待创建的文件(夹),支持多级路径. 若为文件或null返回false; 若目录已存在则返回true;
	 *            The file (clip) to be created to support multilevel paths. Returns false for a file or null; Returns true if the directory already exists;
	 * @return boolean
	 */
	public static boolean createDirectoryRecursively(String directory) {

		if (directory == null) {
			return false;
		}
		File pathname = new File(directory);
		if (pathname.exists()) {
			return !pathname.isFile();
		}
		else if (!pathname.isAbsolute()) {
			pathname = new File(pathname.getAbsolutePath());
		}
		final String parent = pathname.getParent();
		if ((parent == null) || !createDirectoryRecursively(parent)) {
			return false;
		}
		pathname.mkdir();
		return pathname.exists();
	}

	/**
	 * 创建文件，可以包括创建多级文件目录 。
	 * Create a file, which can include creating a multilevel file directory.
	 * <p>
	 * 根据抽象字串文件名新建文件，若文件的上级目录不存在，则先创建目录，再创建文件，返回新文件. 若文件存在,直接返回.
	 * Create a new file based on the abstract string file name, and if the parent directory of the file does not exist, the directory is created, the file is created, and the newly file is returned. If the file exists, return directly.
	 * </p>
	 * 
	 * @param filename
	 *            待创建的文件的抽象文件名称,若为null返回null;若此名称的文件已存在,则直接返回该文件.
	 *            The abstract file name of the file to be created, which returns null if NULL, and returns the file directly if the file with this name already exists.
	 * @return File 创建的文件
	 * 				Files created
	 * @throws IOException
	 */
	public static File createFile(final String filename) throws IOException {

		if (filename == null) {
			return null;
		}
		else {
			return createFile(new File(filename));
		}
	}

	/**
	 * 创建文件，可以包括创建多级文件目录
	 * Create a file that can include creating a multilevel file directory
	 * <p>
	 * 由文件对象创建文件，若文件的上级目录不存在，则先创建目录，再创建文件，返回新文件. 若文件存在,直接返回.
	 * A file is created by a file object, and if the parent directory of the file does not exist, the directory is created, the file is created, and the new file is returned. If the file exists, return directly.
	 * </p>
	 * 
	 * @param file
	 *            待创建的文件
	 *            Files to be created
	 * @return File 创建的文件
	 * 				Files created
	 * @throws IOException
	 */
	public static File createFile(final File file) throws IOException {

		if (!file.exists()) {
			createDirectoryRecursively(file.getParent());
			file.createNewFile();
		}
		return file;
	}

	/**
	 * 从指定的抽象文件路径名称复制文件（夹）到抽象名称的目标文件.
	 * Copies the file (clip) from the specified abstract file path name to the destination file of the abstract name.
	 * 
	 * @param from
	 *            待复制的文件(夹), 为null将不处理
	 *            Files (clips) to be copied, NULL will not be processed
	 * @param to
	 *            目标文件(夹),为null将不处理
	 *            Destination file (clip), NULL will not be processed
	 * @throws IOException
	 */
	public static void copy(String fromFilename, String toFilename) throws IOException {

		if (fromFilename != null && toFilename != null) {
			copy(new File(fromFilename), new File(toFilename));
		}
	}

	/**
	 * 
	 * 复制文件（夹）到目标文件(夹)
	 * Copy file (clip) to destination file (clip)
	 * @param from
	 *            待复制的文件(夹)
	 *            Files to be copied (clips)
	 * @param to
	 *            目标文件(夹)
	 *            Destination file (clip)
	 * @throws IOException
	 * 
	 */
	public static void copy(final File from, final File to) throws IOException {
		if (!from.exists())
			return;
		if (from.isFile()) {
			copyFile(from, to);
		}
		else {
			copyDirectiory(from, to);
		}

	}

	/**
	 * 复制输入流到目标文件,即将输入流写入到存储.
	 * Copy the input stream to the destination file and write the input stream to the store.
	 * 
	 * @param inputStream
	 *            文件输入流
	 *            File input stream
	 * @param to
	 *            目标文件夹
	 *            Destination Folder
	 * @throws IOException
	 */
	public static void copyFileforJava(final InputStream inputStream, final File to) throws IOException {
		createFile(to);
		OutputStream outputStream = null;

		try {
			// copy inputStream to outputStream
			outputStream = new BufferedOutputStream(new FileOutputStream(to));
			if (inputStream != null && outputStream != null) {
				final byte[] buf = new byte[8 * 1024];
				for (;;) {
					final int numRead = inputStream.read(buf);
					if (numRead == -1) {
						break;
					}
					outputStream.write(buf, 0, numRead);
				}
				outputStream.flush();
			}

		}
		finally {
			if (inputStream != null) {
				inputStream.close();
			}
			if (outputStream != null) {
				outputStream.close();
			}
		}

	}

	/**
	 * 
	 * 复制文件到目标文件,若目标文件含有多级路径,则先创建路径,再复制文件到路径
	 * Copy the file to the destination file, and if the destination file contains a multilevel path, create the path first, and then copy the file to the path
	 * <p>
	 * 这个文件复制方法是通过java.nio.channels.FileChannel中的transferTo方法来实现的,速度比较快.<br />
	 * This method of file replication is achieved through the Transferto method in Java.nio.channels.FileChannel, which is faster.
	 * 
	 * 注意文件名,复制后文件名即目标文件,在使用时注意文件扩展名
	 *  Note the file name, after copying the file name is the target file, pay attention to the file name extension when using
	 * </p>
	 * 
	 * @param from
	 *            待复制的文件
	 *            Files to be copied
	 * @param to
	 *            目标文件
	 *            Destination file
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 * 
	 * @see java.nio.channels.FileChannel#transferTo(long, long, java.nio.channels.WritableByteChannel)
	 * 
	 */
	@SuppressWarnings("resource")
	public static long copyFile(File from, File to) throws IOException, FileNotFoundException {
		if (!from.exists()) {
			return -1;
		}
		createFile(to);
		FileChannel fcin = new FileInputStream(from).getChannel();
		FileChannel fcout = new FileOutputStream(to).getChannel();
		long size = fcin.size();

		// 分批次向目标管道输入数据,每批次2MB | Input data into the target pipeline in batches, 2MB per batch
		int length = 2097152;
		while (true) {
			if (fcin.position() == size) {
				fcin.close();
				fcout.close();
				return size;
			}
			if ((size - fcin.position()) < 2097152) {
				length = (int) (size - fcin.position());
			}
			fcin.transferTo(fcin.position(), length, fcout);
			fcin.position(fcin.position() + length);
		}
	}

	/**
	 * 
	 * 复制文件目录(包括子目录和文件)到目标路径
	 * Copy file directories, including subdirectories and files, to the destination path
	 * 
	 * @param sourceDir
	 *            源文件路径
	 *            Source file path
	 * @param targetDir
	 *            目标文件路径
	 *            Destination file path
	 * @throws IOException
	 * 
	 */
	public static void copyDirectiory(final File sourceDir, final File targetDir) throws IOException {
		final File[] files = sourceDir.listFiles();
		if (files == null) {
			return;
		}

		// 创建目标目录 | Create a destination directory
		String targetPath = targetDir.getAbsolutePath();
		createDirectoryRecursively(targetPath);

		for (int i = 0; i < files.length; i++) {
			final File file = files[i];
			if (file.isDirectory()) {
				copyDirectiory(file, new File(targetPath + File.separator + file.getName()));
			}
			else {
				copyFile(file, new File(targetPath + File.separator + file.getName()));
			}
		}
	}

	/**
	 * 
	 * 将文件（夹）移动到目标文件夹
	 * Move the file (folder) to the destination folder
	 * 
	 * @param fromFilename
	 * @param toFilename
	 * @throws IOException
	 * 
	 */
	public static void move(final String fromFilename, final String toFilename) throws IOException {
		if (fromFilename == null || toFilename == null) {
			return;
		}
		else {
			move(new File(fromFilename), new File(toFilename));
		}
	}

	/**
	 * 
	 * 将文件（夹）移动到目标文件夹
	 * Move a file (clip) to the destination folder
	 * 
	 * @param from
	 *            待移动的文件(夹)
	 *            Files to be moved (clips)
	 * @param to
	 *            目标文件夹
	 *            Destination Folder
	 * @throws IOException
	 * 
	 */
	public static void move(final File from, final File to) throws IOException {
		copy(from, to);
		delete(from);
	}

	private static long KB = 1024;
	private static long MB = KB * 1024;
	private static long GB = MB * 1024;

	/**
	 * 将以特殊字串表示的文件大小 (如1.52GB)转换为以long表示文件byte大小
	 * Converts a file size, such as 1.52GB, represented by a special string to a long representation of the file byte size
	 * 
	 * <pre>
	 * example:
	 *   "1.53KB"      --> 153
	 * </pre>
	 * 
	 * @param size
	 *            待转换的以特殊字串表示的文件大小,形式如: 3282B/1.5KB/3.45MB/9.6GB等
	 *            File size to be converted in a special string, in the form of: 3282B/1.5KB/3.45MB/9.6GB, etc.
	 * @return long
	 */
	public static long toFileSize(String size) {

		if (size != null) {
			size = size.toUpperCase();
			final StringBuilder sb = new StringBuilder();
			final StringBuilder sb2 = new StringBuilder();
			for (final char c : size.toCharArray()) {
				if (c == 'B' || c == 'K' || c == 'M' || c == 'G') {
					sb2.append(c);
					continue;
				}
				sb.append(c);
			}
			char c;
			if (sb2.length() == 0 || (c = sb2.charAt(0)) == 'B') {
				// ${todo} 此外转化可深化和优化 | ${todo} In addition, conversion can be deepened and optimized
				return Long.parseLong(sb.toString());
			}
			else {
				final double l = Double.parseDouble(sb.toString());
				if (c == 'G') {
					return (long) (GB * l);
				}
				else if (c == 'M') {
					return (long) (MB * l);
				}
				else if (c == 'K') {
					return (long) (KB * l);
				}
			}
		}
		return 0;
	}

	/**
	 * 返回文件(夹)总大小,包括子文件目录及文件,若为文件夹则计算其下所有文件大小并返回
	 * Returns the total size of the file (clip), including the child file directory and files, and calculates all the file sizes under it for the folder and returns
	 * 
	 * @param directory
	 *            文件(夹),若为null则返回-1;
	 *            File (clip), if NULL, returns-1;
	 * @return 文件大小,单位long
	 * 		   File size, Unit long
	 */
	public static long sizeOfDirectory(final String directory) {
		if (directory == null) {
			return -1;
		}
		return sizeOfDirectory(new File(directory));
	}

	/**
	 * 返回文件(夹)总大小,若为文件夹则计算其下所有文件大小并返回
	 * Returns the total size of the file (clip), calculates all file sizes under it for the folder, and returns
	 * 
	 * @param directory
	 *            文件(夹),若为null则返回-1l;
	 *            File (clip), if NULL, returns -1L;
	 * @return 文件大小,单位long
	 * 			File size, Unit long
	 */
	public static long sizeOfDirectory(final File directory) {

		if (!directory.exists()) {
			return -1;
		}
		if (!directory.isDirectory()) {
			return directory.length();
		}

		long size = 0;
		final File[] files = directory.listFiles();
		if (files == null) {
			return 0l;
		}
		for (int i = 0; i < files.length; i++) {
			final File file = files[i];
			if (file.isDirectory()) {
				size += sizeOfDirectory(file);
			}
			else {
				size += file.length();
			}
		}
		return size;
	}

	/**
	 * 删除此文件或文件夹及其下的所有文件及子文件夹,并删除文件夹本身.
	 * Delete this file or folder and all the files and subfolders under it, and delete the folder itself.
	 * 
	 * @param dir
	 *            待删除的文件(夹)
	 *            Files to be deleted (clips)
	 */
	public static void delete(final File dir) {

		delete(dir, true);
	}

	/**
	 * 删除文件（夹），选项：是否包括本身
	 * Delete file (clip), option: Does it include itself
	 * 
	 * @param dir
	 *            待删除的文件(夹)
	 *            Files to be deleted (clips)
	 * @param self
	 *            若dir是文件夹,self表示是否删除文件夹本身
	 *             If Dir is a folder, self indicates whether to delete the folder itself
	 */
	public static void delete(final File dir, final boolean self) {
		if (!dir.exists()) {
			return;
		}
		if (!dir.isDirectory()) {
			dir.delete();
			return;
		}

		final String[] list = dir.list();
		if (list != null) {
			for (final String element : list) {
				final File child = new File(dir, element);
				delete(child);
			}
		}
		if (self) {
			dir.delete();
		}
	}

}
