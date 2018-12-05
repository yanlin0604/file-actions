package com.file_io;

public class FileUtilTest {

	
	public static void main(String[] args) {
		String filename = "D:\\io-nio\\apache-jmeter-5.0.zip";
		System.out.println(FileUtil.stripFilenameExtension(filename));
	}
}
