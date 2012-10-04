package fi.tut.collabmerge;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.vaadin.aceeditor.SelectionChangeListener;
import org.vaadin.aceeditor.collab.CollabDocAceEditor;
import org.vaadin.aceeditor.collab.DocDiff;
import org.vaadin.aceeditor.collab.gwt.shared.Doc;
import org.vaadin.aceeditor.gwt.shared.Marker;
import org.vaadin.diffsync.Shared;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.VerticalLayout;

import fi.tut.collabmerge.Conflict.ResolvedListener;

@SuppressWarnings("serial")
public class ConflictWidget extends Panel /*implements SelectionChangeListener*/ {
	
	private VerticalLayout layout = new VerticalLayout();
//	private CollabDocAceEditor editor;
	private HashMap<String, Component> tabsByMarkerId = new HashMap<String, Component>();
	private String selectedMarker;
	private Accordion tabs = new Accordion();
	private List<Conflict> conflicts;
	private String mergerName;
	private String mergeHeadName;
	private FileTabSheet sheet;
	private CollabDocAceEditor selectedEditor;
	
//	public ConflictWidget() {
//
//	}
	
	public ConflictWidget(List<Conflict> conflicts, String mergerName, String mergeHeadName, FileTabSheet sheet) {
		super("Conflicts ("+conflicts.size()+")");
		
		this.conflicts = conflicts;
		this.mergerName = mergerName;
		this.mergeHeadName = mergeHeadName;
		this.sheet = sheet;
//		sheet.addListener(new SelectedTabChangeListener() {
//			
////			@Override
////			public void selectedTabChange(SelectedTabChangeEvent event) {
////				event.getComponent();
////				selectedEditor = sheet.getFilenameOf(event.getComponent());
////			}
//		});
		
		drawConflicts();
		layout.addComponent(tabs);
		setContent(layout);
	}
	
	private void drawConflicts() {
		tabs.removeAllComponents();
		int i = 0;
		for (Conflict c : conflicts) {
			newTab(c, i++);
		}
	}

//	public void listenToEditor(CollabDocAceEditor editor) {
//		if (editor==this.editor) {
//			return;
//		}
//		this.editor = editor;
//		editor.addListener(this);
//	}
//	
//	public void connectTo(FileTabSheet sheet) {
//		this.sheet = sheet;
//	}

//	@Override
//	public void selectionChanged(int start, int end) {
//		int smaller = Math.min(start,end);
//		int bigger = Math.max(start,end);
//		LinkedList<String> touchingMarkers = new LinkedList<String>();
//		Map<String, Marker> markers = editor.getShadow().getMarkers();
//		for (Entry<String, Marker> e : markers.entrySet()) {
//			final Marker m = e.getValue();
//			
//			if (m.touches(smaller, bigger)) {
//				touchingMarkers.add(e.getKey());
//			}
//		}
//		
//		LinkedList<String> removed = new LinkedList<String>();
//		for (String mid : tabsByMarkerId.keySet()) {
//			if (!markers.containsKey(mid)) {
//				removed.add(mid);
//			}
//		}
//		
//		
//		if (touchingMarkers.size()>0 && !touchingMarkers.contains(selectedMarker)) {
//			selectedMarker = touchingMarkers.getFirst();
//			tabs.setSelectedTab(tabsByMarkerId.get(selectedMarker));
//		}
//	
//	}
	
	public void sese(CollabDocAceEditor editor, int start, int end) {
		int smaller = Math.min(start,end);
		int bigger = Math.max(start,end);
		LinkedList<String> touchingMarkers = new LinkedList<String>();
		Map<String, Marker> markers = editor.getShadow().getMarkers();
		for (Entry<String, Marker> e : markers.entrySet()) {
			final Marker m = e.getValue();
			
			if (m.touches(smaller, bigger)) {
				touchingMarkers.add(e.getKey());
			}
		}
		
	
		
		
		if (touchingMarkers.size()>0 && (selectedEditor!=editor || !touchingMarkers.contains(selectedMarker))) {
			selectedEditor = editor;
			selectedMarker = touchingMarkers.getFirst();
			String filename = sheet.getFilenameOf(selectedEditor);
			System.out.println("selectedMarker="+selectedMarker);
			tabs.setSelectedTab(tabsByMarkerId.get(filename+":"+selectedMarker));
		}
	
	}

	private Component newTab(final Conflict conflict, final int i) {
		final String markerId = conflict.getMarkerId();
		final ConflictTab tab = new ConflictTab(conflict, mergerName, mergeHeadName);
		
		final CollabDocAceEditor editor = sheet.getEditor(conflict.getFilename());
		
		tab.getScrollButton().addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				if (editor!=null) {
					sheet.setSelectedTab(editor);
					String mid = ((ConflictTab)tabs.getSelectedTab()).getConflict().getMarkerId();
					editor.scrollToMarkerId(mid);
				}
			}
		});
		
		tab.getMineButton().addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				if (editor!=null) {
					Doc doc = editor.getShared().getValue();
					DocDiff dd = MergeUtil.replaceMarkerContentDiff(doc, conflict.getMarkerId(), conflict.getMine());
					editor.getShared().applyDiff(dd, Shared.NO_COLLABORATOR_ID);
				}
			}
		});
		
		tab.getTheirsButton().addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				if (editor!=null) {
					Doc doc = editor.getShared().getValue();
					DocDiff dd = MergeUtil.replaceMarkerContentDiff(doc, conflict.getMarkerId(), conflict.getTheirs());
					editor.getShared().applyDiff(dd, Shared.NO_COLLABORATOR_ID);
				}
			}
		});
		
		tab.getResolveButton().addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				editor.getShared().applyDiff(DocDiff.removeMarker(markerId));
				conflict.resolve();
			}
		});
		final String filename = conflict.getFilename();
		tabsByMarkerId.put(filename+":"+markerId, tab);
		tabs.addTab(tab, conflict.getFilename()+": " + conflict.getMine());
		
		conflict.addListener(new ResolvedListener() {
			@Override
			public void resolved() {
				tabResolved(tab);
			}
		});
		
		if (conflict.isResolved()) {
			tabResolved(tab);
		}
		
		
		editor.addListener(new SelectionChangeListener() {
			
			@Override
			public void selectionChanged(int start, int end) {
				System.out.println("Selection changed in "+filename+" | " + start+"-"+end);
				sese(editor, start, end);
			}
		});
		
		return tab;
	}

	public void tabResolved(ConflictTab tab) {
		tabs.getTab(tab).setCaption("RESOLVED");
		tab.setEnabled(false);
	}

}
