package fi.tut.collabmerge;

import java.util.HashMap;

public class MergeResult {
	
	private final HashMap<String, String> files = new HashMap<String, String>();
	
	public void addFile(String filename, String text) {
		files.put(filename, text);
	}
	
	public String getFile(String filename) {
		return files.get(filename);
	}
	
}
