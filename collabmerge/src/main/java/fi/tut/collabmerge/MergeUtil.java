package fi.tut.collabmerge;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.vaadin.aceeditor.collab.DocDiff;
import org.vaadin.aceeditor.collab.gwt.shared.Doc;
import org.vaadin.aceeditor.collab.gwt.shared.MarkerDiff;
import org.vaadin.aceeditor.gwt.shared.Marker;
import org.vaadin.diffsync.Shared;
import org.vaadin.diffsync.TextDiff;

public class MergeUtil {

	public static class DocWithConflicts {
		public final Doc doc;
		public final List<Conflict> conflicts;
		DocWithConflicts(Doc doc, List<Conflict> conflicts) {
			this.doc = doc;
			this.conflicts = conflicts;
		}
	}
	
	private static Random rnd = new Random();
	private static ConcurrentHashMap<String, MergeAuthor> allAuthKeys = new ConcurrentHashMap<String, MergeAuthor>();
	private static ConcurrentHashMap<String, Collection<String>> mergerCollaborators = new ConcurrentHashMap<String, Collection<String>>();

	
	public static String newMerge(String mergeText, String filename, List<Author> authors) {
		DocWithConflicts dwc = docAndConflictsFromMergeText(mergeText);
		Shared<Doc, DocDiff> st = new Shared<Doc, DocDiff>(dwc.doc);
		Merge m = new Merge(st, filename, authors, dwc.conflicts);
		return createAuthFor(m, authors);
	}

	synchronized private static String createAuthFor(Merge m, List<Author> authors) {
		String mergerAuthKey = null;
		LinkedList<String> otherAuthKeys = new LinkedList<String>();
		for (Author a : authors) {
			String authKey = addMergeAuthor(new MergeAuthor(m, a));
			if (a.isMerger) {
				mergerAuthKey = authKey;
			} else {
				otherAuthKeys.add(authKey);
			}
		}
		addMergerCollaborators(mergerAuthKey, otherAuthKeys);
		return mergerAuthKey;
	}

	public static String waitForMerge(String authKey) {
		MergeAuthor ma = getMergeAuthor(authKey);
		if (ma == null || !ma.author.isMerger) {
			return null;
		}

		String finalText = ma.merge.awaitFinalText();
		destroyMerge(authKey);
		return finalText;
	}

	private synchronized static void destroyMerge(String authKey) {
		// TODO
	}

	private static int debug_i = 0;
	private synchronized static String addMergeAuthor(MergeAuthor ma) {
		while (true) {
			String key = "" + (++debug_i);
			//String key = new BigInteger(16, rnd).toString(Character.MAX_RADIX);
			// String key = Long.toString(Math.abs(rnd.nextLong()) % 1000L,
			// Character.MAX_RADIX).toUpperCase();
			if (allAuthKeys.putIfAbsent(key, ma) == null) {
				return key;
			}
		}
	}

	public synchronized static MergeAuthor getMergeAuthor(String authKey) {
		return allAuthKeys.get(authKey);
	}


	private synchronized static void addMergerCollaborators(String auth, Collection<String> colls) {
		mergerCollaborators.put(auth, colls);
	}

	public synchronized static Collection<String> getMergerCollaborators(String auth) {
		return mergerCollaborators.get(auth);
	}

	public static DocWithConflicts docAndConflictsFromMergeText(String mergeText) {
		StringBuilder sb = new StringBuilder();

		HashMap<String, Marker> markers = new HashMap<String, Marker>();
		LinkedList<Conflict> conflicts = new LinkedList<Conflict>();

		int i = 0;
		int k = mergeText.indexOf("<<<<<<<");
		while (k != -1) {
			sb.append(mergeText.substring(i, k));
			int ms = mergeText.indexOf("\n", k) + 1;
			int me = mergeText.indexOf("\n=======", ms);

			String my = mergeText.substring(ms, me);

			int os = mergeText.indexOf("\n", me + 1) + 1;
			int oe = mergeText.indexOf("\n>>>>>>>", os);

			String others = mergeText.substring(os, oe);

			String markerId = "m" + i;
			markers.put(markerId, Marker.newAceMarker(sb.length(), sb.length()
					+ my.length(), "conflictmarker", "text", false));
			sb.append(my);

			Conflict c = new Conflict(my, others, markerId);
			conflicts.add(c);

			i = mergeText.indexOf("\n", oe + 1);
			if (i==-1) {
				i = mergeText.length();
			}
			k = mergeText.indexOf("<<<<<<<", i);
		}
		sb.append(mergeText.substring(i));

		return new DocWithConflicts(new Doc(sb.toString(), markers), conflicts);
	}
	
	public static DocDiff replaceMarkerContentDiff(Doc doc, String markerId, String newContent) {
		Marker m = doc.getMarkers().get(markerId);
		
		System.err.println("mmm "+m);
		if (m==null) {
			return null;
		}
		
		String oldText = doc.getText();
		String newText = oldText.substring(0,m.getStart()) +
						 newContent +
						 oldText.substring(m.getEnd());
		
		TextDiff td = TextDiff.diff(oldText, newText);
		
		int grew = newContent.length() - (m.getEnd()-m.getStart());
		
		MarkerDiff md = MarkerDiff.create(0, grew);
		
		return DocDiff.create(td, DocDiff.NO_ADDED, Collections.singletonMap(markerId, md), DocDiff.NO_REMOVED);
	}
}
