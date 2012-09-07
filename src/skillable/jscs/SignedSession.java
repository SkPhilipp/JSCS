package skillable.jscs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignedObject;
import java.util.HashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

public class SignedSession extends HashMap<String, Object> {

	private final Long timestamp;

	public Long getTimestamp() {
		return timestamp;
	}

	public SignedSession() {
		this.timestamp = System.currentTimeMillis();
	}

	// -- Static data -------------------------------------

	private static final long serialVersionUID = 1L;
	private static final String CookieName = "SignedSession";
	private static final PrivateKey Private;
	private static final PublicKey Public;
	private static final Signature PrivateSig;
	private static final Signature PublicSig;

	static {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
			keyGen.initialize(1024);
			KeyPair pair = keyGen.generateKeyPair();
			Private = pair.getPrivate();
			Public = pair.getPublic();
			PrivateSig = Signature.getInstance(Private.getAlgorithm());
			PublicSig = Signature.getInstance(Public.getAlgorithm());
		} catch (Exception e) {
			e.printStackTrace();
			throw new AssertionError("Aborting.");
		}
	}

	// -- Serialization and deserialization ---------------

	public Cookie toCookie() throws GeneralSecurityException, IOException {
		SignedObject signedObject = new SignedObject(this, Private, PrivateSig);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(signedObject);
		oos.close();
		return new Cookie(CookieName, DatatypeConverter.printBase64Binary(baos
				.toByteArray()));
	}

	public static SignedSession fromCookie(Cookie cookie) throws IOException,
			ClassNotFoundException, GeneralSecurityException {
		byte[] data = DatatypeConverter.parseBase64Binary(cookie.getValue());
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ObjectInputStream ois = new ObjectInputStream(bais);
		SignedObject signedObject = (SignedObject) ois.readObject();
		if (signedObject.verify(Public, PublicSig)) {
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
