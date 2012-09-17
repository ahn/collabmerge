package fi.tut.collabmerge;

import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.vaadin.aceeditor.collab.DocDiff;
import org.vaadin.aceeditor.collab.gwt.shared.Doc;
import org.vaadin.chatbox.SharedChat;
import org.vaadin.diffsync.Shared;
//import org.vaadin.codeeditor.collab.ide.gwt.shared.Chat;
//import org.vaadin.codeeditor.collab.ide.gwt.shared.ChatDiff;


class Merge {
	


	private static int latestId = 0;
	private static synchronized String newId() {
		latestId++;
		return ""+latestId;
	}
	
	final private String id;
	final private Shared<Doc,DocDiff> merged;
	final private CountDownLatch completedLatch;
	final private String filename;
	final private List<Author> authors;
	final private List<Conflict> conflicts;
	final private SharedChat chat = new SharedChat();
	private String finalText;
	//..

	public Merge(Shared<Doc,DocDiff> text, String filename, List<Author> authors, List<Conflict> conflicts) {
		super();
		this.id = newId();
		this.merged = text;
		this.filename = filename;
		this.authors = authors;
		this.completedLatch = new CountDownLatch(1);
		this.conflicts = conflicts;
		System.err.println("new Merge "+id + "\n" + merged);
	}
	
	public String getId() {
		return id;
	}
	
	public String getFilename() {
		return filename;
	}

	public CountDownLatch getCompletedLatch() {
		return completedLatch;
	}
	
	public Shared<Doc,DocDiff> getShared() {
		return merged;
	}
	
	public Author getConflictCreator() {
		return authors.get(0);
	}
	
	public Author getMergeHeadAuthor() {
		return authors.get(1);
	}
	
	public List<Author> getAuthors() {
		return authors;
	}

	synchronized public List<Conflict> getConflicts() {
		return conflicts;
	}
	
	synchronized public int numConflicts() {
		return conflicts.size();
	}
	
	synchronized public int numResolvedConflicts() {
		int n = 0;
		for (Conflict c : conflicts) {
			if (c.isResolved()) n++;
		}
		return n;
	}
	
	synchronized public boolean conflictsResolved() {
		for (Conflict c : conflicts) {
			if (!c.isResolved()) {
				return false;
			}
		}
		return true;
	}
	
	synchronized public void resolveConflict(Conflict c) {
		c.resolve();
	}
	
	// ????
	public void makeReady() {
		this.finalText = merged.getValue().getText();
		completedLatch.countDown();
		fireCompleted(true);
	}
	
	// ????
	public void makeFailed() {
		completedLatch.countDown();
		fireCompleted(false);
	}
	
	public String awaitFinalText() {
		try {
			completedLatch.await();
		} catch (InterruptedException e) {
			return null;
		}
		
		return finalText;
	}

	public boolean isCompleted() {
		try {
			return getCompletedLatch().await(0, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			return false;
		}
		
	}
	
	public SharedChat getChat() {
		return chat;
	}
	
	
	public interface CompletedListener {
		public void completed(boolean merged);
	}
	private CopyOnWriteArraySet<CompletedListener> listeners = new CopyOnWriteArraySet<Merge.CompletedListener>();
	public void addListener(CompletedListener li) {
		listeners.add(li);
	}
	
	private void fireCompleted(boolean merged) {
		for (CompletedListener li : listeners) {
			li.completed(merged);
		}
	}
}