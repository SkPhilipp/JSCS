Java Signed Cookie Sessions
===========================

Because HttpSession is horrible, especially in Google AppEngine. Why store all data server side,
trying to keep every session in sync across multiple instances, caches, databases, when you
can safely store all this data on the client's side!

_Inspired by PlayFramework's handling of sessions and Tornado's secure cookies._

Example
-------
```java

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import skillable.jscs.ObjectSigner;
import skillable.jscs.SignedSession;

@SuppressWarnings("serial")
public class JscsServlet extends HttpServlet {

	private static final ObjectSigner signer = new ObjectSigner("My seed for the PRNG");

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		SignedSession session = SignedSession.get(req, signer);
		if(session.get("user") != null){
			resp.getWriter().print("Welcome back, " + session.get("user"));
		}
		else{
			session.put("user", "philipp@skillable.eu");
			session.save(resp, signer);
			resp.getWriter().print("Welcome");
		}
	}

}
```