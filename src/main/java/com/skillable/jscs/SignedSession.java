package com.skillable.jscs;

import java.util.HashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

public class SignedSession extends HashMap<String, Object> {

	private static final long serialVersionUID = 1L;

	private final Long timestamp;
	private transient String cookieName;
	private transient ObjectSigner signer;

	/**
	 * Returns the timestamp of when the session was created
	 */
	public Long getTimestamp() {
		return timestamp;
	}

	public SignedSession(String cookieName, ObjectSigner signer) {
		this.timestamp = System.currentTimeMillis();
		this.cookieName = cookieName;
		this.signer = signer;
	}

	@Override
	public String toString() {
		try {
			byte[] data = this.signer.toBytes(this);
			return DatatypeConverter.printBase64Binary(data);
		} catch (Exception e) {
			return null;
		}
	}

	public Cookie toCookie() {
		String string = this.toString();
		return string == null ? null : new Cookie(this.cookieName, string);
	}

	public void addCookie(HttpServletResponse resp) {
		resp.addCookie(this.toCookie());
	}

	protected void setCookieName(String cookieName) {
		this.cookieName = cookieName;
	}

	protected void setSigner(ObjectSigner signer) {
		this.signer = signer;
	}

}
