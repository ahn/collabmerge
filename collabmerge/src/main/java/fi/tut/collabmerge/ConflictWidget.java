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
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

import fi.tut.collabmerge.Conflict.ResolvedListener;

@SuppressWarnings("serial")
public class ConflictWidget extends Panel implements SelectionChangeListener {
	
	private VerticalLayout layout = new VerticalLayout();
	private CollabDocAceEditor editor;
	private HashMap<String, Component> tabsByMarkerId = new HashMap<String, Component>();
	private String selectedMarker;
	private Accordion tabs = new Accordion();
	private List<Conflict> conflicts;
	private Merge merge;
	
	public ConflictWidget() {

	}
	
	public ConflictWidget(Merge merge) {
		super("Conflicts ("+merge.numConflicts()+")");
		
		this.merge = merge;
		this.conflicts = merge.getConflicts();
		
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

	public void listenToEditor(CollabDocAceEditor editor) {
		if (editor==this.editor) {
			return;
		}
		this.editor = editor;
		editor.addListener(this);
	}

	@Override
	public void selectionChanged(int start, int end) {
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
		
		LinkedList<String> removed = new LinkedList<String>();
		for (String mid : tabsByMarkerId.keySet()) {
			if (!markers.containsKey(mid)) {
				removed.add(mid);
			}
		}
		
		
		if (touchingMarkers.size()>0 && !touchingMarkers.contains(selectedMarker)) {
			selectedMarker = touchingMarkers.getFirst();
			tabs.setSelectedTab(tabsByMarkerId.get(selectedMarker));
		}
	
	}

	private Component newTab(final Conflict conflict, final int i) {
		final String markerId = conflict.getMarkerId();
		final ConflictTab tab = new ConflictTab(conflict, merge);
		
		tab.getScrollButton().addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				if (editor!=null) {
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
					System.err.println("ddddd " + dd);
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
				editor.getShared().applyDiff(DocDiff.removeMarker(markerId), editor.getCollaboratorId());
				merge.resolveConflict(conflict);
			}
		});
		tabsByMarkerId.put(markerId, tab);
		tabs.addTab(tab, conflict.getMine());
		
		conflict.addListener(new ResolvedListener() {
			@Override
			public void resolved() {
				tabResolved(tab);
			}
		});
		
		if (conflict.isResolved()) {
			tabResolved(tab);
		}
		
		return tab;
	}

	public void tabResolved(ConflictTab tab) {
		tabs.getTab(tab).setCaption("RESOLVED");
		tab.setEnabled(false);
	}

}
