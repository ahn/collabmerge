package fi.tut.collabmerge;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;

import org.vaadin.aceeditor.collab.CollabDocAceEditor;
import org.vaadin.aceeditor.gwt.ace.AceMode;
import org.vaadin.chatbox.ChatBox;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import fi.tut.collabmerge.Conflict.ResolvedListener;
import fi.tut.collabmerge.Merge.CompletedListener;

@SuppressWarnings("serial")
public class CMWidget extends HorizontalSplitPanel {

	private VerticalLayout editorLayout;
	private VerticalLayout sideBar;

	private CollabDocAceEditor ace;
	private Label mergeInfoLabel = new Label();

	private final MergeAuthor mergeAuthor;
	private final String authKey;

	private Button readyButton = new Button("Apply Merge!"); {
		readyButton.setWidth("100%");
		readyButton.setEnabled(false);
	}

	private Button cancelButton = new Button("Cancel Merge"); {
		cancelButton.setWidth("100%");
	}

	private URL baseURL; 
	
	public CMWidget(String authKey, URL baseURL) {
		super();
		setSplitPosition(300, Sizeable.UNITS_PIXELS, true);
		this.authKey = authKey;
		this.mergeAuthor = MergeUtil.getMergeAuthor(authKey);
		this.baseURL = baseURL;
		draw();
	}

	@Override
	public void attach() {
		super.attach();
		readyButton.addListener(new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				readyButton.setEnabled(false);
				cancelButton.setEnabled(false);
				mergeAuthor.merge.makeReady();
			}
		});
		cancelButton.addListener(new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				mergeAuthor.merge.makeFailed();
			}
		});
		
		mergeAuthor.merge.addListener(new CompletedListener() {
			@Override
			public void completed(boolean merged) {
				CMWidget.this.setEnabled(false);
				Window w = getWindow();
				if (w!=null) {
					w.showNotification(merged?"Merge Applied!":"Merge Cancelled");
				}
			}
		});
		
		for (Conflict c : mergeAuthor.merge.getConflicts()) {
			c.addListener(new ResolvedListener() {
				@Override
				public void resolved() {
					updateResolved();
				}
			});
		}
	}

	private void addButtons() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");

		hl.addComponent(cancelButton);
		hl.addComponent(readyButton);
		hl.setExpandRatio(cancelButton, 1);
		hl.setExpandRatio(readyButton, 2);
		sideBar.addComponent(hl);
	}
	
	private void foo2() {
		Collection<String> friendAuths = MergeUtil.getMergerCollaborators(authKey);
		for (String ak : friendAuths) {
			sideBar.addComponent(createInviteComponent(ak));
		}
	}

	private void draw() {
		setCaption(mergeAuthor.author.name + " "
				+ mergeAuthor.merge.getFilename());
		
		this.setSizeFull();
		
		editorLayout = new VerticalLayout();
		editorLayout.setSizeFull();
		this.addComponent(editorLayout);

		sideBar = new VerticalLayout();
		sideBar.setSizeFull();
		this.addComponent(sideBar);

//		this.setExpandRatio(editorLayout, 2);
//		this.setExpandRatio(sideBar, 1);

		ace = new CollabDocAceEditor(mergeAuthor.merge.getShared());
		AceMode mode = AceMode.forFile(mergeAuthor.merge.getFilename());
		if (mode != null) {
			ace.setMode(
					mode,
					"http://antti.virtuallypreinstalled.com/cored/VAADIN/widgetsets/org.vaadin.codeeditor.gwt.AceEditorWidgetset/ace/mode-"
							+ mode.toString() + ".js");
		}

		ConflictWidget cw = new ConflictWidget(mergeAuthor.merge);
		cw.listenToEditor(ace);
		sideBar.addComponent(cw);
		sideBar.addComponent(new Label("&nbsp;", Label.CONTENT_XHTML));

		sideBar.addComponent(mergeInfoLabel);

		if (mergeAuthor.author.isMerger) {
			addButtons();
		}
		sideBar.addComponent(new Label("&nbsp;", Label.CONTENT_XHTML));

		sideBar.addComponent(new Label("Chat:"));
		ChatBox cb = new ChatBox(mergeAuthor.merge.getChat());
		cb.setUser(mergeAuthor.author.name, mergeAuthor.author.name, "keke");
		cb.setWidth("100%");
		sideBar.addComponent(cb);

		sideBar.setExpandRatio(cb, 1);

		ace.setPollInterval(1000);
		ace.setSizeFull();
		editorLayout.addComponent(ace);
		editorLayout.setExpandRatio(ace, 1);
				
		if (mergeAuthor.author.isMerger) {
			foo2();
		}
		
		updateResolved();
	}

	private Component createInviteComponent(String authKey) {
		Author a = MergeUtil.getMergeAuthor(authKey).author;
		Panel pa = new Panel(a.isMergeHead?("Invite " + a.name):"Invite");
		VerticalLayout la = new VerticalLayout();
		if (a.isMergeHead) {
			la.addComponent(new Label("Give this URL to " + a.name + ":"));
		}
		else {
			la.addComponent(new Label("Give this URL to somebody who could help:"));
		}
		
		la.addComponent(new Label(authURL(authKey)));
		
		if (!a.isMergeHead) {
			URI doodle = doodleURI(mergeAuthor.author.name, mergeAuthor.author.email,
						mergeAuthor.merge.getFilename(), authURL(authKey));

			la.addComponent(new Link("Schedule with Doodle!", new ExternalResource(doodle.toASCIIString())));
		}
		pa.setContent(la);
		return pa;
	}
	
	private static URI doodleURI(String name, String email, String file, String authURL) {
		
		String query;
		try {
			System.err.println(URLEncoder.encode("@", "UTF-8"));
			query = "type=date&locale=en&title=Resolve%20Conflict%20Together"+ 
					"&name="+URLEncoder.encode(name, "UTF-8")+"&eMailAddress="+email+"&jee=joo";
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return null;
		}

		try {
			return new URI("http", null, "//doodle.com/polls/wizard.html", query, null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
//		
//				+email+"&description=Hi,%0A%0AI%20encountered%20a%20conflict%20when%20trying%20to%20merge%20the%20changes%20in%20file%20"+file+".%20I'm%20not%20sure%20how%20to%20resolve%20the%20conflict.%0A%0AIt'd%20be%20great%20if%20you%20guys%20could%20help%20me%20resolve%20it.%20See%20you%20at%0A%0A"+authURL+"&location="+authURL;
	}
	
	private String authURL(String auth) {
		return baseURL + "?auth=" + auth;
	}

	public void updateResolved() {
		int tot = mergeAuthor.merge.numConflicts();
		int res = mergeAuthor.merge.numResolvedConflicts();
		mergeInfoLabel.setValue("Conflits Resolved: " + res + "/" + tot);
		readyButton.setEnabled(tot == res);
	}
}
