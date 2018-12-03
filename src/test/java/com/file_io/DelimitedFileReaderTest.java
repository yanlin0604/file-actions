package com.file_io;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;



public class DelimitedFileReaderTest {

	public static void main(String[] args) {
		DelimitedFileReaderTest delimitedFileReaderTest = new DelimitedFileReaderTest();
		delimitedFileReaderTest.createFileReader().evalOutDatedVersions();
	}
	private DelimitedFileReader createFileReader(){
		try{
			final Path path = Paths.get("src/main/resources", "input.txt");
			final Reader reader = Files.newBufferedReader(path, Charset.forName("UTF-8"));
			return new DelimitedFileReader(reader, ",");
			
		}catch(final IOException e){
			throw new UncheckedIOException(e);
		}
	}
}
