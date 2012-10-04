package fi.tut.collabmerge;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.vaadin.aceeditor.collab.DocDiff;
import org.vaadin.aceeditor.collab.gwt.shared.Doc;
import org.vaadin.chatbox.SharedChat;
import org.vaadin.diffsync.Shared;


public class MultiMerge {
	
	private static int latestId = 0;
	private static synchronized String newId() {
		latestId++;
		return ""+latestId;
	}

	final private String id;
	
	//private HashMap<String, FileMerge> merges = new HashMap<String, FileMerge>();
	
	final private List<Author> authors;
	
	private List<Conflict> conflicts = new LinkedList<Conflict>();

	private Map<String, Shared<Doc, DocDiff>> docs = new HashMap<String, Shared<Doc,DocDiff>>();
	
	final private CountDownLatch completedLatch = new CountDownLatch(1);

	private MergeResult result;
	
	final private SharedChat chat = new SharedChat();
	
	public MultiMerge(List<Author> authors) {
		this.id = newId();
		this.authors = authors;
	}
	
	
	public String getId() {
		return id;
	}
//	
//	public void addMerge(FileMerge m) {
//		merges.put(m.getFilename(), m);
//	}
//	
//	public Collection<FileMerge> getMerges() {
//		return merges.values();
//	}
	
	public void addFile(String filename, String mergeText) {
		addFile(filename, MergeUtil.docAndConflictsFromMergeText(filename, mergeText));
	}
	
	public synchronized void addFile(String filename, DocWithConflicts dwc) {
		conflicts.addAll(dwc.conflicts);
		Shared<Doc, DocDiff> sh = new Shared<Doc,DocDiff>(dwc.doc);
		docs.put(filename, sh);
	}
	
	public synchronized Shared<Doc, DocDiff> getDoc(String filename) {
		return docs.get(filename);
	}
	
	public synchronized Set<String> getFileNames() {
		return new HashSet<String>(docs.keySet());
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
	synchronized public void makeReady() {
		result = new MergeResult();
		for (Entry<String, Shared<Doc, DocDiff>> e : docs.entrySet()) {
			result.addFile(e.getKey(), e.getValue().getValue().getText());
		}
		completedLatch.countDown();
		fireCompleted(true);
	}
	
	// ????
	synchronized public void makeFailed() {
		completedLatch.countDown();
		fireCompleted(false);
	}
	
	public MergeResult awaitMergeResult() {
		try {
			completedLatch.await();
		} catch (InterruptedException e) {
			return null;
		}
		
		return result;
	}

	public boolean isCompleted() {
		try {
			return completedLatch.await(0, TimeUnit.MILLISECONDS);
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
	private CopyOnWriteArraySet<CompletedListener> listeners = new CopyOnWriteArraySet<CompletedListener>();
	public void addListener(CompletedListener li) {
		listeners.add(li);
	}
	
	private void fireCompleted(boolean merged) {
		for (CompletedListener li : listeners) {
			li.completed(merged);
		}
	}
}
