package fi.tut.collabmerge;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.AbstractApplicationServlet;

@SuppressWarnings("serial")
public class CMServlet extends AbstractApplicationServlet {

	@Override
	public void init(javax.servlet.ServletConfig servletConfig)
			throws javax.servlet.ServletException {
		super.init(servletConfig);
	}

	@Override
	protected Application getNewApplication(HttpServletRequest request)
			throws ServletException {
		return new CMApplication();
	}

	@Override
	protected Class<? extends Application> getApplicationClass()
			throws ClassNotFoundException {
		return CMApplication.class;
	}

	@Override
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if (request.getParameter("upload") != null) {			
			
			String filename = request.getParameter("filename");
			String mergeText = request.getParameter("filecontent");
			
			LinkedList<Author> authors = new LinkedList<Author>();
			for (int ai = 0; true; ai++) {
				String anp = "author" + ai;
				if (request.getParameter(anp) == null) {
					break;
				}
				String name = request.getParameter(anp);
				String email = request.getParameter(anp + "email");
				authors.add(new Author(name, email, ai == 0, ai == 1));
			}
			
			String auth = request.getParameter("auth");
			if (auth==null) {
				auth = MergeUtil.newMerge(authors);
			}
			
			MergeAuthor ma = MergeUtil.getMergeAuthor(auth);
			if (ma.author.isMerger) {
				ma.merge.addFile(filename, mergeText);
				// ???
				response.addHeader("Content-Type", "text/plain;charset="+ Charset.defaultCharset().toString());
				response.getWriter().println("auth="+auth);
			}
			else {
				System.err.println("!!!!!!!!!!!!!!!!!!!!");
			}
			
		} else if (request.getParameter("download") != null) {
			String authKey = request.getParameter("auth");
			String filename = request.getParameter("filename");
			String s = MergeUtil.getMergeResultForFile(authKey, filename);

			// ???
			response.addHeader("Content-Type", "text/plain;charset="
					+ Charset.defaultCharset().toString());

			PrintWriter out = response.getWriter();

			if (s == null) {
				out.println("FAILURE");
			} else {
				out.println("SUCCESS");
				out.println(s);
			}
		}

		else {
			super.service(request, response);
		}

	}

}
