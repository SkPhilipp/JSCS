Java Signed Cookie Sessions
===========================

_Inspired by PlayFramework's handling of sessions and Tornado's secure cookies._

Created because HttpSession is horrible, especially in Google AppEngine. Why store all data server side,
trying to keep every session in sync across multiple instances, caches, databases, when you
can safely store all this data on the client's side!

[Download .jar here](https://github.com/SkPhilipp/JSCS/downloads)

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

Warnings & Protips
------------------

1. It's a cookie, cookies are sent in headers, headers are sent before bodies. You cant send
a header anymore once you start with the body.

_DO_
```java
session.put("user", "philipp@skillable.eu");
session.save(resp, signer);
resp.getWriter().print("Welcome");
```

_DONT_
```java
session.put("user", "philipp@skillable.eu");
resp.getWriter().print("Welcome");
session.save(resp, signer);
```

2. Don't put sensitive data in it, it's a signed cookie, not an encrypted cookie.
3. Don't put too much data in it, it's a cookie, and cookies are supposed to have a maximum
size of 4KB, the signature takes up quite some space, and Base64 encoding it makes this even longer,
so don't be silly, and keep the cookie small.