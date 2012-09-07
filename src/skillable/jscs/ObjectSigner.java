package skillable.jscs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.SignedObject;

public class ObjectSigner {

	private final PrivateKey Private;
	private final PublicKey Public;
	private final Signature PrivateSig;
	private final Signature PublicSig;

	public ObjectSigner() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			keyGen.initialize(1024, random);
			random.setSeed(1);
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

	/**
	 * Converts an object to a byte array representing a SignedObject which
	 * contains the given serializable object.
	 */
	public byte[] toBytes(Serializable object) throws IOException,
			InvalidKeyException, SignatureException {
		SignedObject signedObject = new SignedObject(object, Private,
				PrivateSig);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(signedObject);
		oos.close();
		return baos.toByteArray();
	}

	/**
	 * Converts a byte array to a SignedObject, verifies this SignedObject and
	 * returns the serializable object contained inside the SignedObject.
	 */
	public Serializable fromBytes(byte[] data) throws IOException,
			ClassNotFoundException, InvalidKeyException, SignatureException {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ObjectInputStream ois = new ObjectInputStream(bais);
		SignedObject signedObject = (SignedObject) ois.readObject();
		if (signedObject.verify(Public, PublicSig)) {
			return (Serializable) signedObject.getObject();
		} else {
			throw new SignatureException("Verification failed.");
		}
	}

}
