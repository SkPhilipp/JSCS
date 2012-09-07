package skillable.jscs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignedObject;
import java.util.HashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SignedSession extends HashMap<String, Object> {

	private static final long serialVersionUID = 1L;

	private final Long timestamp;

	public Long getTimestamp() {
		return timestamp;
	}

	public SignedSession() {
		this.timestamp = System.currentTimeMillis();
	}

	// -- Static data -------------------------------------

	private static final Charset UTF8;
	private static final Signature RSA;
	private static final String CookieName = "SignedSession";
	private static final PrivateKey Private;
	private static final PublicKey Public;

	static {
		try {
			UTF8 = Charset.forName("UTF-8");
			RSA = Signature.getInstance("SHA1withRSA");
			// Generating the Public and Private key
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			keyGen.initialize(1024, random);
			KeyPair pair = keyGen.generateKeyPair();
			Private = pair.getPrivate();
			Public = pair.getPublic();
		} catch (Exception e) {
			throw new AssertionError("SHA1withRSA, or UTF-8 unknown, aborting.");
		}
	}

	// -- Serialization and deserialization ---------------

	public Cookie toCookie() throws GeneralSecurityException, IOException {
		SignedObject signedObject = new SignedObject(this, Private, RSA);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(signedObject);
		oos.flush();
		return new Cookie(CookieName, new String(baos.toByteArray(), UTF8));
	}

	public static SignedSession fromCookie(Cookie cookie) throws IOException,
			ClassNotFoundException, GeneralSecurityException {
		byte[] data = cookie.getValue().getBytes(UTF8);
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ObjectInputStream ois = new ObjectInputStream(bais);
		SignedObject signedObject = (SignedObject) ois.readObject();
		if (signedObject.verify(Public, RSA)) {
			return (SignedSession) signedObject.getObject();
		}
		return null;
	}

	public void save(HttpServletResponse resp) throws IOException,
			GeneralSecurityException {
		resp.addCookie(this.toCookie());
	}

	public static SignedSession load(HttpServletRequest req)
			throws IOException, ClassNotFoundException,
			GeneralSecurityException {
		for (Cookie cookie : req.getCookies()) {
			if (CookieName.equals(cookie.getName())) {
				return SignedSession.fromCookie(cookie);
			}
		}
		return null;
	}

}
