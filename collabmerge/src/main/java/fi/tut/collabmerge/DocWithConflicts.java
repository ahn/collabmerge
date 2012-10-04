package fi.tut.collabmerge;

import java.util.List;

import org.vaadin.aceeditor.collab.gwt.shared.Doc;

public class DocWithConflicts {
	public final Doc doc;
	public final List<Conflict> conflicts;
	DocWithConflicts(Doc doc, List<Conflict> conflicts) {
		this.doc = doc;
		this.conflicts = conflicts;
	}
}