package fi.tut.collabmerge;

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
		
		Label mineLabel = new Label("<strong>"+conflict.getMine()+"</strong>", Label.CONTENT_XHTML);
		mineLabel.setCaption("By "+mergerName+":");
//		mineLabel.setContentMode(Label.CONTENT_PREFORMATTED);
		addComponent(mineLabel);
		addComponent(mineButton);
		addComponent(newSpacer());
		
		Label theirsLabel = new Label("<strong>"+conflict.getTheirs()+"</strong>", Label.CONTENT_XHTML);
		theirsLabel.setCaption("FileMerge Head (by "+mergeHeadName+")");
//		theirsLabel.setContentMode(Label.CONTENT_PREFORMATTED);
		addComponent(theirsLabel);
		addComponent(theirsButton);
		addComponent(newSpacer());

		
		addComponent(resolveButton);
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
}
