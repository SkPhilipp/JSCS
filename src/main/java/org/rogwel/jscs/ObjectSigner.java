package org.rogwel.jscs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.SignedObject;

public class ObjectSigner {
	private final KeyPairGenerator keyGen;
	private final SecureRandom random;

	private PrivateKey Private;
	private PublicKey Public;
	private Signature PrivateSig;
	private Signature PublicSig;

	public ObjectSigner(String seedString) {
		try {
			keyGen = KeyPairGenerator.getInstance("DSA");
			random = SecureRandom.getInstance("SHA1PRNG");
			seed(seedString);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AssertionError("DSA or SHA1PRNG unknown, aborting.");
		}
	}

	public ObjectSigner() {
		this(null);
	}

	/**
	 * Seeds the PRNG and generates a new KeyPair
	 */
	public void seed(String seed) {
		if (seed != null) {
			random.setSeed(seed.getBytes());
		}
		keyGen.initialize(1024, random);
		KeyPair pair = keyGen.generateKeyPair();
		Private = pair.getPrivate();
		Public = pair.getPublic();
		try {
			PrivateSig = Signature.getInstance(Private.getAlgorithm());
			PublicSig = Signature.getInstance(Public.getAlgorithm());
		} catch (NoSuchAlgorithmException e) {
			// Can't even happen here.
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
