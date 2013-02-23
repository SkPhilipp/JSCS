package org.rogwel.jscs;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;

public class SignedSessionFactory {

	private final String cookieName;
	private final ObjectSigner signer;
	private final Long expiration;

	/**
	 * @param signerSeed
	 *            Seed for the CSPRNG.
	 * @param cookieName
	 *            Name used for toCookie and fromCookie methods.
	 * @param expiration
	 *            Expiration time in milliseconds, used in verifying a
	 *            SignedSession's timestamp. 604800000L is one week. Using 0L
	 *            means expiration time will not be checked.
	 */
	public SignedSessionFactory(String signerSeed, String cookieName, Long expiration) {
		this.cookieName = cookieName;
		this.signer = new ObjectSigner(signerSeed);
		this.expiration = expiration;
	}

	public String getCookieName() {
		return cookieName;
	}

	public ObjectSigner getSigner() {
		return signer;
	}

	public SignedSession createSession() {
		return new SignedSession(cookieName, signer);
	}

	public SignedSession fromCookie(Cookie cookie) {
		return fromString(cookie.getValue());
	}

	public SignedSession fromString(String string) {
		try {
			byte[] bytes = DatatypeConverter.parseBase64Binary(string);
			SignedSession session = (SignedSession) signer.fromBytes(bytes);
			if (expiration != 0 && System.currentTimeMillis() - session.getTimestamp() > expiration) {
				return null;
			} else {
				session.setCookieName(cookieName);
				session.setSigner(signer);
				return session;
			}
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Loads from an HttpServletRequest, or creates a new SignedSession
	 */
	public SignedSession get(HttpServletRequest req) {
		SignedSession session = find(req);
		return session == null ? new SignedSession(cookieName, signer) : session;
	}

	/**
	 * Loads from an HttpServletRequest, or returns null.
	 */
	public SignedSession find(HttpServletRequest req) {
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookieName.equals(cookie.getName())) {
					return fromCookie(cookie);
				}
			}
		}
		return null;
	}

}
