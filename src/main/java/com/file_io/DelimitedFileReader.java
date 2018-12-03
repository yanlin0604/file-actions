package com.file_io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * File reader interface
 * @author Juanjuan
 * @param <T>
 */
@FunctionalInterface
interface FileReader<T>{
	
	/**
	 * Trigger file reading
	 * 
	 * @return
	 * @author Juanjuan
	 */
	List<T> readFile();
}


/**
 * Software inventory delimited file reader
 * @author Juanjuan
 */
public class DelimitedFileReader implements FileReader<Software>{

	final Reader source;

	final String recordFieldDelimeter;

	final GroupingService groupingService = new GroupingService();

	/**
	 * Construct {@link DelimitedFileReader}
	 * @param source
	 * @param delimeter
	 */
	protected DelimitedFileReader(final Reader source, final String delimeter) {
		this.source = source;
		this.recordFieldDelimeter = delimeter;
	}

	/**
	 * Read 
	 * 
	 * @return
	 * @author Juanjuan
	 */
	public List<Software> readFile(){
		try (BufferedReader reader = new BufferedReader(this.source)){
			return reader.lines().map(line -> new Software(line.split(this.recordFieldDelimeter))).collect(Collectors.toList());
		}catch(final IOException e){
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Traverse through all installed softwares across all servers and prepare report of servers with out dated softwares
	 * 
	 * @author Juanjuan
	 */
	protected void evalOutDatedVersions(){
		final Map<String, List<Software>> grouped = this.groupingService.groupRecordsBySoftware(this.readFile());
		final List<String> outDated = new ArrayList<>();
		grouped.forEach((k, v) -> {
			Collections.sort(v);
			v.stream().skip(1).filter(s -> !outDated.contains(s.getServer())).forEach(s -> outDated.add(s.getServer()));
		});
		System.out.println(outDated);
		try{
				Files.write(Paths.get("src/main/resources", "out.txt"),outDated);
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

}

/**
 * Policy factory to enforce genuineness of data
 * @author Juanjuan
 */
enum DataPolicy {
	MYSQL("Database","(\\d+)\\.(\\d+)"), UBUNTU("OS","(\\d+)\\.(\\d+)"), PYTHON("Language","(\\d+)\\.(\\d+).(\\d+)");

	private String type;
	private String regExp;

	private DataPolicy(final String type, final String regExp) {
		this.type = type;
		this.regExp = regExp;
	}

	String getRegExp(){
		return this.regExp;
	}
	
	String getType(){
		return this.type;
	}

	public static DataPolicy getSoftwareVersion(final String name){
		for(final DataPolicy v : DataPolicy.values()){
			if(v.name().equalsIgnoreCase(name)){
				return v;
			}
		}
		return null;
	}

	public boolean isValidVersion(final String value){
		return Pattern.compile(this.getRegExp()).matcher(value).matches();
	}
}

/**
 * Grouping service to group software list
 * @author Juanjuan
 */
class GroupingService {

	/**
	 * Group installed software and their versions
	 * 
	 * @param entities
	 * 			the list of install softwares
	 * @return
	 * 			the map representing Software and corresponding versions installed
	 * 
	 * @author Juanjuan
	 */
	public Map<String, List<Software>> groupRecordsBySoftware(final List<Software> entities){
		final Map<String, List<Software>> hashMap = new HashMap<>();
		for(final Software s : entities){
			if(!hashMap.containsKey(s.getName())){
				final List<Software> list = new ArrayList<>();
				list.add(s);
				hashMap.put(s.getName(), list);
			}else{
				final List<Software> list = hashMap.get(s.getName());
				if(!list.get(list.size() -1).getVersion().equals(s.getVersion())){
					hashMap.get(s.getName()).add(s);
				}
			}
		}

		return hashMap;
	}
}

/**
 * Bean representing each record on the file
 * @author Juanjuan
 */
class Software implements Comparable<Software> {

	private String server;

	private String type;

	private String name;

	private String version;

	public Software(final String[] values) {
		this.server = values[0].trim();
		this.type = values[1].trim();
		this.name = values[2].trim();
		this.version = values[3].trim();
		if(!DataPolicy.getSoftwareVersion(this.name).isValidVersion(this.version)){
			throw new IllegalArgumentException("Malformed version");
		}
	}

	/**
	 * @return the server
	 */
	public String getServer(){
		return this.server;
	}

	/**
	 * @param server the server to set
	 */
	public void setServer(final String server){
		this.server = server;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(final String version){
		this.version = version;
	}

	/**
	 * @return the type
	 */
	public String getType(){
		return this.type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(final String type){
		this.type = type;
	}

	/**
	 * @return the name
	 */
	public String getName(){
		return this.name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(final String name){
		this.name = name;
	}

	/**
	 * @return the version
	 */
	public String getVersion(){
		return this.version;
	}

	@Override
	public int compareTo(final Software o){
		return Integer.valueOf(Arrays.asList(o.getVersion().split("\\.")).stream().mapToInt(part -> Integer.valueOf(part)).sum())
				.compareTo(Integer.valueOf(Arrays.asList(this.version.split("\\.")).stream().mapToInt(part -> Integer.valueOf(part)).sum()));
	}
}

