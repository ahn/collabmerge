package fi.tut.collabmerge;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.LinkedList;

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
		if (request.getParameter("initmerge") != null) {
			String mergeText = request.getParameter("initmergetext");
			String filename = request.getParameter("filename");

			LinkedList<Author> authors = new LinkedList<Author>();
			for (int ai = 0; true; ai++) {
				String anp = "author" + ai;
				if (request.getParameter(anp) == null) {
					System.err.println("NO PARAM " + anp);
					break;
				}
				String name = request.getParameter(anp);
				String email = request.getParameter(anp + "email");
				authors.add(new Author(name, email, ai == 0, ai == 1));
			}
			String authKey = MergeUtil.newMerge(mergeText, filename, authors);

			// ???
			response.addHeader("Content-Type", "text/plain;charset="
					+ Charset.defaultCharset().toString());
			// response.addHeader("Content-Type",
			// "text/plain;charset="+request.getCharacterEncoding());

			response.getWriter().println(authKey);
		} else if (request.getParameter("getmerge") != null) {
			String authKey = request.getParameter("auth");
			String s = MergeUtil.waitForMerge(authKey);

			// ???
			response.addHeader("Content-Type", "text/plain;charset="
					+ Charset.defaultCharset().toString());
			// response.addHeader("Content-Type",
			// "text/plain;charset="+request.getCharacterEncoding());

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
