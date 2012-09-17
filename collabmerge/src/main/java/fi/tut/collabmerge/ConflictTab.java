package fi.tut.collabmerge;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

@SuppressWarnings("serial")
public class ConflictTab extends VerticalLayout {
	
	private final Conflict conflict;
	private final Merge merge;
	private final Button resolveButton = new Button("Mark As Resolved");
	private final Button scrollButton = new Button("Scroll To Conflict");
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
	
	public ConflictTab(Conflict conflict, Merge merge) {
		super();
		this.conflict = conflict;
		this.merge = merge;
		draw();
	}
	
	private void draw() {
		
		addComponent(scrollButton);
		addComponent(newSpacer());
		
		Label mineLabel = new Label("<strong>"+conflict.getMine()+"</strong>", Label.CONTENT_XHTML);
		mineLabel.setCaption("By "+merge.getConflictCreator().name+":");
//		mineLabel.setContentMode(Label.CONTENT_PREFORMATTED);
		addComponent(mineLabel);
		addComponent(mineButton);
		addComponent(newSpacer());
		
		Label theirsLabel = new Label("<strong>"+conflict.getTheirs()+"</strong>", Label.CONTENT_XHTML);
		theirsLabel.setCaption("Merge Head (by "+merge.getMergeHeadAuthor().name+")");
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
