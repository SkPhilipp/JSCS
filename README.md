Java Signed Cookie Sessions
===========================

_This project is no longer maintained and part of my source code "attic". Feel free to use it though, works fine._

[![Build Status](https://travis-ci.org/Rogwel/JSCS.png?branch=master)](https://travis-ci.org/Rogwel/JSCS)

Created because sessions are horrid when it comes to having more than one instance of an application running. Why store all data server side, trying to keep every session in sync across multiple setrver instances, caches, and databases, when you can securely store session data, on the client’s side in a cookie.
You can find the source code at the Github Repository. Feel free to contribute!- JSCS is Licensed under the Apache License 2.0

### Example

    import java.io.IOException;
    import javax.servlet.http.HttpServlet;
    import javax.servlet.http.HttpServletRequest;
    import javax.servlet.http.HttpServletResponse;
    import org.rogwel.jscs.SignedSessionFactory ;
    import org.rogwel.jscs.SignedSession;
    
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
                session.put("user", "philipp");
                session.addCookie(resp);
                resp.getWriter().print("Welcome");
            }
        }
    
    }

### Notes
- It’s a cookie, cookies are sent in headers, headers are sent before the body. You cant send a header anymore once you start with the body.
- Don’t put sensitive data in it, it’s a signed cookie, not an encrypted cookie.
- Don’t put too much data in it, it’s a cookie, and cookies are supposed to have a maximum size of 4KB, the signature takes up quite some space, encoding the cookie into a safe format (base64) makes it even longer, so don’t be silly, and keep the cookie small.
- Sessions will not be lost when your webserver, cache, or database go down. Or when you deploy a new version of your application.
- The cookie that used to be an identifier now is the session, this means when a user deletes the session cookie to get rid of a session, no stray sessions will be hanging around in your database, meaning you don’t have to have session-timeout mechanisms. This makes it much easier for the user because cookies can last indefinately, meaning a user can stay logged in forever.
- The session cookie can contain verification data, or versioning data, meaning you are still in charge of what session data is allowed and what isn’t. Also, the cookie is encrypted, so only the server can read it.
