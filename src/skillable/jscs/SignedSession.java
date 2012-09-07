package skillable.jscs;

import java.util.HashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SignedSession extends HashMap<String, Object> {

	private static final long serialVersionUID = 1L;
	private static final ObjectSigner signer = new ObjectSigner();

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

	/**
	 * Converts this object to a Cookie
	 */
	public Cookie toCookie() {
		try {
			return new Cookie(CookieName, new String(signer.toBytes(this)));
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Loads a SignedSession from a Cookie
	 */
	public static SignedSession fromCookie(Cookie cookie) {
		try {
			return (SignedSession) signer.fromBytes(cookie.getValue()
					.getBytes());
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Stores this object into a Cookie which is then added to the given
	 * HttpServletResponse.
	 */
	public void save(HttpServletResponse resp) {
		try {
			resp.addCookie(this.toCookie());
		} catch (Exception e) {
		}
	}

	/**
	 * Loads a SignedSession from an HttpServletRequest.
	 */
	public static SignedSession load(HttpServletRequest req) {
		for (Cookie cookie : req.getCookies()) {
			if (CookieName.equals(cookie.getName())) {
				return SignedSession.fromCookie(cookie);
			}
		}
		return null;
	}

}
