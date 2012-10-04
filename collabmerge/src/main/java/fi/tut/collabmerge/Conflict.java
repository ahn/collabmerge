package fi.tut.collabmerge;

import java.util.LinkedList;


public class Conflict {
	private final String mine;
	private final String theirs;
	private final String markerId;
	private final String filename;
	private boolean resolved = false;
	public Conflict(String filename, String markerId, String mine, String theirs) {
		this.filename = filename;
		this.markerId = markerId;
		this.mine = mine;
		this.theirs = theirs;
	}
	public String getMine() {
		return mine;
	}
	public String getTheirs() {
		return theirs;
	}
	public String getMarkerId() {
		return markerId;
	}
	public String getFilename() {
		return filename;
	}
	
	public boolean isResolved() {
		return resolved;
	}
	
	public void resolve() {
		if (!resolved) {
			resolved = true;
			for (ResolvedListener li : listeners) {
				li.resolved();
			}
		}
	}
	
	public interface ResolvedListener {
		public void resolved();
	}
	private LinkedList<ResolvedListener> listeners = new LinkedList<ResolvedListener>();
	public void addListener(ResolvedListener li) {
		listeners.add(li);
	}
}
