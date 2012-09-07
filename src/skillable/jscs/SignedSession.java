package skillable.jscs;

import java.util.HashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

	public Cookie toCookie(ObjectSigner signer) {
		try {
			return new Cookie(CookieName, new String(signer.toBytes(this)));
		} catch (Exception e) {
			return null;
		}
	}

	public static SignedSession fromCookie(Cookie cookie, ObjectSigner signer) {
		try {
			SignedSession session = (SignedSession) signer.fromBytes(cookie
					.getValue().getBytes());
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
	 * given ObjectSigner, returns null if there is no SignedSession active.
	 */
	public static SignedSession load(HttpServletRequest req, ObjectSigner signer) {
		for (Cookie cookie : req.getCookies()) {
			if (CookieName.equals(cookie.getName())) {
				return SignedSession.fromCookie(cookie, signer);
			}
		}
		return null;
	}

	/**
	 * Loads a SignedSession from an HttpServletRequest, verifies it with the
	 * given ObjectSigner.
	 */
	public static SignedSession load(HttpServletRequest req,
			ObjectSigner signer, boolean create) {
		for (Cookie cookie : req.getCookies()) {
			if (CookieName.equals(cookie.getName())) {
				return SignedSession.fromCookie(cookie, signer);
			}
		}
		if (create) {
			return new SignedSession();
		} else {
			return null;
		}
	}

}
