package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;

public class EncryptionHandler {

	private KeyPair myKeys;
	private TreeMap<Byte, PublicKey> pubKeys = new TreeMap<Byte,PublicKey>();
	
	public EncryptionHandler() {
		try {
			myKeys = generateKeyPair(1024);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public EncryptionHandler(String filename) {
		File privateKeyFile = new File("/home/joeyjo0/"+filename+".pem");
		File publicKeyFile = new File("/home/joeyjo0/"+filename+".pub");
		try {
			if(!privateKeyFile.exists() || !publicKeyFile.exists()) {
				RandomAccessFile privateKeyRAF = new RandomAccessFile(privateKeyFile, "rw");
				RandomAccessFile publicKeyRAF = new RandomAccessFile(publicKeyFile, "rw");
				
				KeyPair kp = generateKeyPair(1024);
				
				privateKeyRAF.setLength(0);
				privateKeyRAF.write("-----BEGIN RSA PRIVATE KEY-----\n".getBytes());
				privateKeyRAF.write(DatatypeConverter.printBase64Binary(kp.getPrivate().getEncoded()).getBytes());
				privateKeyRAF.write("\n-----END RSA PRIVATE KEY-----".getBytes());
				
				publicKeyRAF.setLength(0);
				publicKeyRAF.write("-----BEGIN RSA PUBLIC KEY-----\n".getBytes());
				publicKeyRAF.write(DatatypeConverter.printBase64Binary(kp.getPublic().getEncoded()).getBytes());
				publicKeyRAF.write("\n-----END RSA PUBLIC KEY-----".getBytes());
				
				System.out.println("New keys generated");
			}
			
			myKeys = readKeys(publicKeyFile, privateKeyFile);
			
			//System.out.println(DatatypeConverter.printBase64Binary(myKeys.getPrivate().getEncoded()));
			//System.out.println(DatatypeConverter.printBase64Binary(myKeys.getPublic().getEncoded()));
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
	
	/**
	 * Encrypts a message with a hosts public key.
	 * 
	 * @param 	in Bytes to encrypt
	 * @param 	host The host to take the public key of.
	 * @return	in bytes encrypted with the hosts public key.
	 */
	public byte[] encrypt(byte[] in, byte host) {
		PublicKey pubKey = pubKeys.get(host);
		if(pubKey != null) {
		    try {
		        Cipher rsa;
		        rsa = Cipher.getInstance("RSA");
		        rsa.init(Cipher.ENCRYPT_MODE, pubKey);
		        return rsa.doFinal(in);
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		} else {
			System.out.println("Key for host " + host + " not found. Cannot encrypt.");
		}
	    return null;
	}
	
	/**
	 * Decrypts a message with the private key
	 * 
	 * @param 	in Bytes to decrypt
	 * @return	Decrypted bytes from in
	 */
	public byte[] decrypt(byte[] in) {
	    try {

	        Cipher rsa;
	        rsa = Cipher.getInstance("RSA");
	        rsa.init(Cipher.DECRYPT_MODE, myKeys.getPrivate());
	        return rsa.doFinal(in);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	/**
	 * 
	 * @return
	 */
	public String parsePubKeyPacket(byte[] packet) {
		byte[] pubKey = new byte[(packet.length-7)];
		System.arraycopy(packet, 7, pubKey, 0, pubKey.length);
	    return new String(pubKey);
	}
	
	/**
	 * Fetches the packet for a public key share.
	 * 
	 * @return	The packet used for public key sharing.
	 */
	public byte[] getPubKeyPacket() {
	    StringBuilder sb = new StringBuilder();
	    sb.append("PUBKEY ");
	    sb.append(DatatypeConverter.printBase64Binary(myKeys.getPublic().getEncoded()));
	    return sb.toString().getBytes();
	}
	
	
	/**
	 * Returns the public key for this instance.
	 * 
	 * @return Public key for this instance.
	 */
	public String getPubKey() {
		return DatatypeConverter.printBase64Binary(myKeys.getPublic().getEncoded());
	}
	
	/**
	 * Adds a public key to the list of known keys.
	 * 
	 * @param 	host The host the public key is for.
	 * @param	pubKey The public key of the host.
	 */
	public void addPubKey(Byte host, String pubKey) {
		PublicKey pubKeyObject;
		try {
			pubKeyObject = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(DatatypeConverter.parseBase64Binary(pubKey)));
			this.pubKeys.put(host, pubKeyObject);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Generates a new KeyPair
	 * 
	 * @param	size The size of the new keys, in bits.
	 */
	private KeyPair generateKeyPair(int size) throws NoSuchAlgorithmException {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(size);
        KeyPair keys = keyGen.genKeyPair();
        return keys;
	}
	
	/**
	 * Reads the public and private key from the corresponding files.
	 * 
	 * @param	pubKeyFile The file that contains the public key.
	 * @param 	privKeyFile The file that contains the private key.
	 * @return	The keypair with keys fetched from the files.
	 * @throws 	InvalidKeySpecException
	 * @throws 	NoSuchAlgorithmException
	 * @throws 	IOException
	 */
	private KeyPair readKeys(File pubKeyFile, File privKeyFile) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
		RandomAccessFile pubKeyFileReader = new RandomAccessFile(pubKeyFile, "r");
		RandomAccessFile privKeyFileReader = new RandomAccessFile(privKeyFile, "r");
		int privKeyFileLength = (int)privKeyFileReader.length();
		byte[] privKeyBytes = new byte[privKeyFileLength];
		privKeyFileReader.read(privKeyBytes);
		String privKeyString = new String(privKeyBytes);
		privKeyString = privKeyString.split("\n")[1];
		
		int pubKeyFileLength = (int)pubKeyFileReader.length();
		byte[] pubKeyBytes = new byte[pubKeyFileLength];
		pubKeyFileReader.read(pubKeyBytes);
		String pubKeyString = new String(pubKeyBytes);
		pubKeyString = pubKeyString.split("\n")[1];
		
		PrivateKey privKeyObject = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(DatatypeConverter.parseBase64Binary(privKeyString)));
		PublicKey pubKeyObject = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(DatatypeConverter.parseBase64Binary(pubKeyString)));

		return new KeyPair(pubKeyObject,privKeyObject);
	}
	
}
