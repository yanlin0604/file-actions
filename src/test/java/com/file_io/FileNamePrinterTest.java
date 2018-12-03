package com.file_io;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class FileNamePrinterTest {

	public static void main(String[] args) {
		String fileName = "D:\\io-nio";
		
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
	
		LinkedList<String> foldersQueue = new LinkedList<>();
		List<String> subFoldersList = new ArrayList<>();
		
		//foldersQueue.addAll(getSubFoldersAsList(fileName));
		foldersQueue.add(fileName);
		
		while (!foldersQueue.isEmpty()) {
			String currentFolder = foldersQueue.getLast();
			foldersQueue.addAll(0,getSubFoldersAsList(currentFolder));
			subFoldersList.add(currentFolder);
			foldersQueue.removeLast();
			System.out.println("Folder :: "+currentFolder);
		}
		
		for(String folder : subFoldersList){
			FileNamePrinter fileNamePrinter = new FileNamePrinter(folder);
			executor.execute(fileNamePrinter);
		}
		
		executor.shutdown();
	}

	public static List<String> getSubFoldersAsList(String fileName){
		List<String> subFoldersList = new LinkedList<>();
		
		File[] files = new File(fileName).listFiles();

		if (files != null) {
			for (File file : files) {
				
				if (file.isDirectory()) {
					subFoldersList.add(file.getAbsolutePath());
					
				}
			}
		}
		
		return subFoldersList;
	}
}
