package skillable.jscs;

import java.util.HashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

public class SignedSession extends HashMap<String, Object> {

	private static final long serialVersionUID = 1L;

	private final Long timestamp;

	/**
	 * Returns the timestamp of when the session was created
	 */
	public Long getTimestamp() {
		return timestamp;
	}

	public SignedSession() {
		this.timestamp = System.currentTimeMillis();
	}

	private static String CookieName = SignedSession.class.getSimpleName();

	/**
	 * Sets the name of the cookie, the cookie name is used by the other methods
	 * to find and store the cookie.
	 */
	public static void setCookieName(String newName) {
		if (newName == null || newName.length() == 0) {
			throw new NullPointerException("Name cannot be null or empty");
		}
		SignedSession.CookieName = newName;
	}

	public String toString(ObjectSigner signer) {
		try {
			String string = DatatypeConverter.printBase64Binary(signer
					.toBytes(this));
			return string;
		} catch (Exception e) {
			return null;
		}
	}

	public Cookie toCookie(ObjectSigner signer) {
		String string = this.toString();
		if (string == null) {
			return null;
		} else {
			return new Cookie(CookieName, string);
		}
	}

	public static SignedSession fromCookie(Cookie cookie, ObjectSigner signer) {
		return SignedSession.fromString(cookie.getValue(), signer);
	}

	public static SignedSession fromString(String string, ObjectSigner signer) {
		try {
			byte[] bytes = DatatypeConverter.parseBase64Binary(string);
			SignedSession session = (SignedSession) signer.fromBytes(bytes);
			return session;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Stores this object into a Cookie, signed by the given ObjectSigner, the
	 * cookie is then added to the given HttpServletResponse.
	 */
	public void save(HttpServletResponse resp, ObjectSigner signer) {
		try {
			resp.addCookie(this.toCookie(signer));
		} catch (Exception e) {
		}
	}

	/**
	 * Loads a SignedSession from an HttpServletRequest, verifies it with the
	 * given ObjectSigner, creates a new SignedSession if there is none active.
	 */
	public static SignedSession get(HttpServletRequest req, ObjectSigner signer) {
		SignedSession session = SignedSession.find(req, signer);
		return session == null ? new SignedSession() : session;
	}

	/**
	 * Loads a SignedSession from an HttpServletRequest, verifies it with the
	 * given ObjectSigner, returns null if there is no SignedSession active.
	 */
	public static SignedSession find(HttpServletRequest req, ObjectSigner signer) {
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (CookieName.equals(cookie.getName())) {
					return SignedSession.fromCookie(cookie, signer);
				}
			}
		}
		return null;
	}

}
