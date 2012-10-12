package fi.tut.collabmerge;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

import com.vaadin.Application;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class CMApplication extends Application {

	private Window mainWindow;
	
	// for debugging



	
	
	public CMApplication() {
		super();
		//demoInit();
	}
	
	@Override
	public Window getWindow(String name) {
		Window w = super.getWindow(name);
		if (w == null) {
			System.out.println("new window");
			w = new CMWindow();
			w.setName(name);
			addWindow(w);
		}
		return w;
	}
	
	@Override
	public void init() {
		mainWindow = new CMWindow();

		setMainWindow(mainWindow);
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
