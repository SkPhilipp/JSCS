Java Signed Cookie Sessions
===========================

_Inspired by PlayFramework's handling of sessions and Tornado's secure cookies._

Created because HttpSession is horrible, especially in Google AppEngine. Why store all data server side,
trying to keep every session in sync across multiple instances, caches, databases, when you
can safely store all this data on the client's side!

[Download latest .jar here](https://github.com/SkPhilipp/JSCS/downloads)

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

	private static final SignedSessionFactory factory = new SignedSessionFactory("CSPRNG Seed", "SessionCookie", 0L);
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		SignedSession session = factory.get(req);
		if(session.get("user") != null){
			resp.getWriter().print("Welcome back, " + session.get("user"));
		}
		else{
			session.put("user", "philipp@skillable.eu");
			session.addCookie(resp);
			resp.getWriter().print("Welcome");
		}
	}

}
```

Warning
-------
1. It's a cookie, cookies are sent in headers, headers are sent before the body. You cant send a header anymore once you start with the body.
2. Don't put sensitive data in it, it's a signed cookie, not an encrypted cookie.
3. Don't put too much data in it, it's a cookie, and cookies are supposed to have a maximumsize of 4KB, the signature takes up quite some space, and Base64 encoding it makes this even longer, so don't be silly, keep the cookie small.