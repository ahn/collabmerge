package fi.tut.collabmerge;

import java.net.URL;
import java.util.Map;

import com.github.wolfie.refresher.Refresher;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.ParameterHandler;
import com.vaadin.terminal.URIHandler;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class CMWindow extends Window {
	
	
	private URL myURL;
	private String myAuthKey;
	
	
	public CMWindow() {
		super();
		initialize();
	}
	
	private void initialize() {
		addParameterHandler(new ParameterHandler() {
			public void handleParameters(Map<String, String[]> parameters) {
				String[] items = parameters.get("auth");
				if (items!=null && items.length==1) {
					myAuthKey = items[0];
				}
				else {
					myAuthKey = null;
				}
			}
		});
		addURIHandler(new URIHandler() {
			public DownloadStream handleURI(URL context, String relativeUri) {
				myURL = context;
				startApp();
				return null;
			}
		});
	}
	
	private void startApp() {
		System.err.println("START APP");
		if (myAuthKey == null) {
			showError("No Authentication");
			return;
		}
		MergeAuthor mergeAuthor = MergeUtil.getMergeAuthor(myAuthKey);
		if (mergeAuthor==null) {
			showError("Invalid Authentication");
			return;
		}
		if (mergeAuthor.merge.isCompleted()) {
			showError("The merge is already completed.");
			return;
		}
		
		showWindow(myAuthKey, mergeAuthor);
	}
	
	
	private void showError(String error) {
		setContent(null);
		showNotification(error, Notification.TYPE_ERROR_MESSAGE);
	}
	
	private void showWindow(String authKey, MergeAuthor mergeAuthor) {
		setSizeFull();
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		setContent(layout);
		Refresher ref = new Refresher();
		ref.setRefreshInterval(1000);
		layout.addComponent(ref);
		CMWidget widget = new CMWidget(authKey, myURL);
		widget.setSizeFull();
		layout.addComponent(widget);
		layout.setExpandRatio(widget, 1);
	}
}
