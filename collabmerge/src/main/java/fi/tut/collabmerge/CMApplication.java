package fi.tut.collabmerge;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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


	private URL myURL;
	private String myAuthKey;
	
	
	public CMApplication() {
		super();
		demoInit();
	}
	
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
	}
	
	private void demoInit() {

		LinkedList<Author> authors = new LinkedList<Author>();
		authors.add(new Author("Alice", "alice@example.com", true, false));
		authors.add(new Author("Bob", "bob@example.com", false, true));
		authors.add(new Author("Anonymous", "", false, false));
		String auth = MergeUtil.newMerge(authors);

		String s = "# a python file\n\ndef func()\n" +

		"<<<<<<< HEAD\n" + "\tfunc2()\n" + "=======\n" + "\tfunc1()\n"
				+ ">>>>>>> caccb57504b807a14d35d130b0376eb324d7128b"
				+ "\n\ndef func2():\n" + "\tpass\n\n";
		MergeUtil.getMergeAuthor(auth).merge.addFile("selection_test.js", s);

		String s2 = "# another python file\n\ndef func()\n" +

		"<<<<<<< HEAD\n" + "\tfunc4()\n" + "=======\n" + "\tfunc222()\n"
				+ ">>>>>>> caccb57504b807a14d35d130b0376eb324d7128b"
				+ "\n\ndef func2():\n" + "\tpass\n\n";
		MergeUtil.getMergeAuthor(auth).merge.addFile("range.js", s2);

		InputStream in = getClass().getResourceAsStream("/testconflict.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		StringBuilder sb = new StringBuilder();
		try {
			String li;
			while ((li = br.readLine()) != null) {
				sb.append(li).append("\n");
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String s3 = sb.toString();
		MergeUtil.getMergeAuthor(auth).merge.addFile("selection.js", s3);
	}
}
