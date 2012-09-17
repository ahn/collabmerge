package fi.tut.collabmerge;

import java.util.LinkedList;


public class Conflict {
	private final String mine;
	private final String theirs;
	private final String markerId;
	private boolean resolved = false;
	public Conflict(String mine, String theirs, String markerId) {
		this.mine = mine;
		this.theirs = theirs;
		this.markerId = markerId;
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
