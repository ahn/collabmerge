package fi.tut.collabmerge;

import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.gwt.ace.AceMode;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

@SuppressWarnings("serial")
public class ConflictTab extends VerticalLayout {
	
	private final Conflict conflict;
//	private final MultiMerge mm;
	private String mergerName;
	private String mergeHeadName;
	
	private final Button resolveButton = new Button("Mark as Resolved");
	{
		resolveButton.setWidth("100%");
	}
	private final Button scrollButton = new Button("Go to Conflict");
	{
		scrollButton.setStyleName(BaseTheme.BUTTON_LINK);
	}
	private Button mineButton = new Button("<<< Use");
	{
		mineButton.setStyleName(BaseTheme.BUTTON_LINK);
	}
	private Button theirsButton = new Button("<<< Use");
	{
		theirsButton.setStyleName(BaseTheme.BUTTON_LINK);
	}
	


	private AceEditor mineEditor;
	private AceEditor theirsEditor;
	
	public ConflictTab(Conflict conflict, String mergerName, String mergeHeadName) {
		super();
		this.conflict = conflict;
		this.mergerName = mergerName;
		this.mergeHeadName = mergeHeadName;
		draw();
	}
	
	private void draw() {
		
		addComponent(scrollButton);
		addComponent(newSpacer());
		
		mineEditor = new AceEditor();
		mineEditor.setUseWrapMode(true);
		mineEditor.setCaption(""+mergerName+":");
		mineEditor.setValue(conflict.getMine());
		mineEditor.setMode(AceMode.forFile(conflict.getFilename()));
		mineEditor.setWidth("100%");
		mineEditor.setHeight("100px");
		mineEditor.setReadOnly(true);
		
		addComponent(mineEditor);
		addComponent(mineButton);
		addComponent(newSpacer());
		
		theirsEditor = new AceEditor();
		theirsEditor.setUseWrapMode(true);
		theirsEditor.setCaption("Merge head ("+mergeHeadName+")");
		theirsEditor.setValue(conflict.getTheirs());
		theirsEditor.setMode(AceMode.forFile(conflict.getFilename()));
		theirsEditor.setWidth("100%");
		theirsEditor.setHeight("100px");
		theirsEditor.setReadOnly(true);
		
		addComponent(theirsEditor);
		addComponent(theirsButton);
		addComponent(newSpacer());

		
		addComponent(resolveButton);
		addComponent(newSpacer());
	}

	private static Component newSpacer() {
		return new Label("&nbsp;", Label.CONTENT_XHTML);

	}
	
	public Button getResolveButton() {
		return resolveButton;
	}
	
	public Button getScrollButton() {
		return scrollButton;
	}
	
	public Button getMineButton() {
		return mineButton;
	}
	
	public Button getTheirsButton() {
		return theirsButton;
	}

	public Conflict getConflict() {
		return conflict;
	}
	
	public AceEditor getMineEditor() {
		return mineEditor;
	}

	public AceEditor getTheirsEditor() {
		return theirsEditor;
	}
}
