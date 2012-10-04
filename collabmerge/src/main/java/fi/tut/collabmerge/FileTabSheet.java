package fi.tut.collabmerge;

import java.util.HashMap;

import org.vaadin.aceeditor.collab.CollabDocAceEditor;
import org.vaadin.aceeditor.collab.DocDiff;
import org.vaadin.aceeditor.collab.gwt.shared.Doc;
import org.vaadin.aceeditor.gwt.ace.AceMode;
import org.vaadin.diffsync.Shared;

import com.vaadin.ui.TabSheet;

@SuppressWarnings("serial")
public class FileTabSheet extends TabSheet {
	
	private HashMap<String, CollabDocAceEditor> editors = new HashMap<String, CollabDocAceEditor>();
	
	public FileTabSheet(MultiMerge merge) {
		super();
		setSizeFull();
		
		draw(merge);
	}
	
	public CollabDocAceEditor getSelectedEditor() {
		return (CollabDocAceEditor) getSelectedTab();
	}
	
	public CollabDocAceEditor getEditor(String filename) {
		return editors.get(filename);
	}
	
	public String getFilenameOf(CollabDocAceEditor editor) {
		return getTab(editor).getCaption();
	}
	
	private void draw(MultiMerge merge) {
		for (String f : merge.getFileNames() ) {
			addFile(f, merge.getDoc(f));
		}
	}
	
	private void addFile(String filename, Shared<Doc, DocDiff> shared) {
		CollabDocAceEditor editor = new CollabDocAceEditor(shared);
		editor.setSizeFull();
		editor.setMode(AceMode.forFileEnding(filename.substring(filename.lastIndexOf(".")+1)));
		editors.put(filename, editor);
		addTab(editor, filename);
	}

}
