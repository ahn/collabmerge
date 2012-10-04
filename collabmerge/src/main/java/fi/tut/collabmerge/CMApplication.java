package fi.tut.collabmerge;


import java.net.URL;
import java.util.LinkedList;
import java.util.Map;

import com.github.wolfie.refresher.Refresher;
import com.vaadin.Application;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.ParameterHandler;
import com.vaadin.terminal.URIHandler;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

@SuppressWarnings("serial")
public class CMApplication extends Application {

	private Window mainWindow;
	
	// for debugging
	static {
		LinkedList<Author> authors = new LinkedList<Author>();
		authors.add( new Author("Eka", "eka@example.com", true, false) );
		authors.add( new Author("Toka", "toka@example.com", false, true) );
		authors.add( new Author("Anonymous", "", false, false) );
		String auth = MergeUtil.newMerge(authors);
		
		String s ="# a python file\n\ndef func()\n" +
				
				"<<<<<<< HEAD\n" +
				"\tfunc2()\n" +
				"=======\n" +
				"\tfunc1()\n" +
				">>>>>>> caccb57504b807a14d35d130b0376eb324d7128b" +
				"\n\ndef func2():\n" +
				"\tpass\n\n";
		MergeUtil.getMergeAuthor(auth).merge.addFile("jee.py", s);
		
		String s2 ="# another python file\n\ndef func()\n" +
				
				"<<<<<<< HEAD\n" +
				"\tfunc4()\n" +
				"=======\n" +
				"\tfunc222()\n" +
				">>>>>>> caccb57504b807a14d35d130b0376eb324d7128b" +
				"\n\ndef func2():\n" +
				"\tpass\n\n";
		MergeUtil.getMergeAuthor(auth).merge.addFile("jee2.py", s2);
	}

	private URL myURL;
	private String myAuthKey;
	
	@Override
	public Window getWindow(String name) {
		Window w = super.getWindow(name);
		if (w == null) {
			w = new Window();
			w.setName(name);
			addWindow(w);
		}
		return w;
	}
	
	@Override
	public void init() {
		mainWindow = new Window("collabmerge");
		mainWindow.addParameterHandler(new ParameterHandler() {
			public void handleParameters(Map<String, String[]> parameters) {
				String[] items = parameters.get("auth");
				if (items!=null && items.length==1) {
					myAuthKey = items[0];
				}
			}
		});
		mainWindow.addURIHandler(new URIHandler() {
			public DownloadStream handleURI(URL context, String relativeUri) {
				myURL = context;
				startApp();
				return null;
			}
		});
		setMainWindow(mainWindow);
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
		mainWindow.setContent(null);
		mainWindow.showNotification(error, Notification.TYPE_ERROR_MESSAGE);
	}
	
	
	private void showWindow(String authKey, MergeAuthor mergeAuthor) {
		//mainWindow.setCaption(mergeAuthor.merge.getFilename()+" - "+mergeAuthor.author.name);
		mainWindow.setSizeFull();
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		mainWindow.setContent(layout);
		Refresher ref = new Refresher();
		ref.setRefreshInterval(1000);
		layout.addComponent(ref);
		CMWidget widget = new CMWidget(authKey, myURL);
		widget.setSizeFull();
		layout.addComponent(widget);
		layout.setExpandRatio(widget, 1);
		
//		mainWindow.setContent(new CMWidget(authKey, myURL));
	}
}
