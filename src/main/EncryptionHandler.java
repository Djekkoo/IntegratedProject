package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;

import tests.CryptoTest;

public class EncryptionHandler {

	/**
	 * Contains the public and private key.
	 */
	private KeyPair myKeys;
	/**
	 * Contains the public keys of remote hosts.
	 */
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
		File privateKeyFile = new File(filename+".pem");
		File publicKeyFile = new File(filename+".pub");
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
				
				System.out.println("New keys generated and written to disk.");
			}
			
			myKeys = readKeys(publicKeyFile, privateKeyFile);
			
			System.out.println(DatatypeConverter.printBase64Binary(myKeys.getPrivate().getEncoded()));
			System.out.println(DatatypeConverter.printBase64Binary(myKeys.getPublic().getEncoded()));
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String showKeys() {
		StringBuilder sb = new StringBuilder();
		sb.append("Public keys:\n");
		for(Entry<Byte,PublicKey> e : pubKeys.entrySet()) {
			sb.append("Host: " + e.getKey() + " => " + e.getValue());
		}
		return sb.toString();
	}
	
	/**
	 * Fetches the public key from a received packet.
	 * 
	 * @return The public key contained in the packet
	 */
	public static String parsePubKeyPacket(byte[] packet) {
		byte[] pubKey = new byte[(packet.length-7)];
		System.arraycopy(packet, 7, pubKey, 0, pubKey.length);
	    return new String(pubKey);
	}
	
	/**
	 * Builds a packet from supplied data. Appends the signature in front of the encrypted data.
	 * 
	 * @param 	in The data to encrypt and to make a signature of.
	 * @param 	host The host whose public key should be used to encrypt.
	 * @return	The packet, encrypted and with a signature.
	 */
	public byte[] getPacket(byte[] in, byte host) {
		byte[] encryptedData = this.encrypt(in, host);
		byte[] signedHash = this.sign(in);
		if(encryptedData != null) {
			byte[] finalPacket = new byte[encryptedData.length+signedHash.length+1];
			finalPacket[0] = (byte)0xFF;
			System.arraycopy(signedHash, 0, finalPacket, 1, signedHash.length);
			System.arraycopy(encryptedData, 0, finalPacket, signedHash.length+1, encryptedData.length);
		    return finalPacket;
		} else {
			byte[] finalPacket = new byte[in.length+1];
			finalPacket[0] = (byte)0x00;
			System.arraycopy(in, 0, finalPacket, 1, in.length);
			return finalPacket;
		}
		
		
		
	}
	
	/**
	 * Takes a packet, decrypts the data and validates the signature.
	 * 
	 * @param 	in An encrypted packet.
	 * @param 	host The host the packet is from.
	 * @return	The unencrypted data contained by the packet.
	 * @throws CryptoException 
	 */
	public byte[] parsePacket(byte[] in, byte host) throws CryptoException {
		byte[] signedHash = new byte[128];
		if(in[0] == (byte)0xFF) {
			System.out.println("Decrypting!");
			byte[] newIn = new byte[in.length-1];
			System.arraycopy(in, 1, newIn, 0, newIn.length);
			
			byte[] encryptedData = new byte[newIn.length-128];
			//We took the first byte off here
			System.arraycopy(newIn, 0, signedHash, 0, 128);
			System.arraycopy(newIn, 128, encryptedData, 0, encryptedData.length);
			
			byte[] decryptedData = this.decrypt(encryptedData);
			boolean validated = this.validate(host, decryptedData, signedHash);
			if(validated) {
				return decryptedData;
			} else {
				throw new CryptoException("Cannot validate packet.");
			}
		} else {
			System.out.println("No need to decrypt!");
			byte[] out = new byte[in.length-1];
			System.arraycopy(in, 1, out, 0, out.length);
			return out;
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
		int iterations = 1;
		int modulo = 0;
		if(in.length > 117) {
			iterations = (int)Math.ceil(in.length/117);
			iterations++;
		}
		modulo = in.length % 117;
		byte[] cipherText = new byte[iterations*128];
		PublicKey pubKey = pubKeys.get(host);
		if(pubKey != null) {
			for(int i = 0; i < iterations; i++) {
				byte[] toBeEncrypted;
				if(iterations == i+1) {
					toBeEncrypted = new byte[modulo];
					System.arraycopy(in, (i*117), toBeEncrypted, 0, modulo);
				} else {
					toBeEncrypted = new byte[117];
					System.arraycopy(in, (i*117), toBeEncrypted, 0, 117);
				}
			    try {
			        Cipher rsa;
			        rsa = Cipher.getInstance("RSA");
			        rsa.init(Cipher.ENCRYPT_MODE, pubKey);
			        byte[] tempCipherText = rsa.doFinal(toBeEncrypted);
			        System.arraycopy(tempCipherText, 0, cipherText, 128*i, 128);
			    } catch (Exception e) {
			        e.printStackTrace();
			    }
			}
			return cipherText;
		} else {
			System.out.println("Key for host " + host + " not found. Cannot encrypt.");
			return null;
		}
	}
	
	/**
	 * Decrypts a message with the private key
	 * 
	 * @param 	in Bytes to decrypt
	 * @return	Decrypted bytes from in
	 */
	public byte[] decrypt(byte[] in) {
		int iterations = 1;
		if(in.length > 128) {
			iterations = (int)Math.ceil(in.length/128);
		}
		int  messageLen = 0;
		byte[] message = new byte[iterations * 117];
	    try {
	    	for(int i = 0; i < iterations; i++){
	    		byte[] toBeDecrypted = new byte[128];
	    		System.arraycopy(in, i*128, toBeDecrypted, 0, 128);
	    		Cipher rsa;
	 	        rsa = Cipher.getInstance("RSA");
	 	        rsa.init(Cipher.DECRYPT_MODE, myKeys.getPrivate());
	 	        byte[] tempMessage = rsa.doFinal(toBeDecrypted);
	 	        System.arraycopy(tempMessage, 0, message, messageLen, tempMessage.length);
	 	        messageLen += tempMessage.length;
	    	}
	    	byte[] finalMessage = new byte[messageLen];
	    	System.arraycopy(message, 0, finalMessage, 0, messageLen);
	    	return finalMessage;
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	/**
	 * Signs a packet, takes the checksum of the payload, then encrypts it with the private key.
	 * 
	 * @param 	in The payload to take the checksum of.
	 * @return	The checksum, encrypted with the private key.
	 */
	public byte[] sign(byte[] in) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
	
			byte[] hash = digest.digest(in);
			
			PrivateKey privKey = myKeys.getPrivate();
	        Cipher rsa;
	        rsa = Cipher.getInstance("RSA");
	        rsa.init(Cipher.ENCRYPT_MODE, privKey);
	        byte[] sign = rsa.doFinal(hash);
	        return sign;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return null;
	}
	
	/**
	 * Validates a packet signature, checks if it's signed by the correct host.
	 * 
	 * @return	True if the signature is correct.
	 * 			False if the signature is invalid.
	 */
	public boolean validate(Byte host, byte[] data, byte[] hmac) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] checkHash = digest.digest(data);
	    	PublicKey pubKey = pubKeys.get(host);
	    	if(pubKey != null) {
		        Cipher rsa;
		        rsa = Cipher.getInstance("RSA");
		        rsa.init(Cipher.DECRYPT_MODE, pubKey);
		        byte[] realHash = rsa.doFinal(hmac);
		        
		        boolean correct = true;
		        int i = 0;
		        for(byte hashByte : realHash) {
		        	if(hashByte != checkHash[i]) {
		        		correct = false;
		        	}
		        	i++;
		        }
		        return correct;
	    	} else {
	    		System.out.println("Key for host " + host + " not found. Cannot validate.");
	    	}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return false;
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
	 * Checks if a public key for a given host exists.
	 * 
	 * @return 	True is the public key exists in the table.
	 * 			False if no public key is known.
	 */
	public boolean pubKeyExists(byte host) {
		return this.pubKeys.containsKey(host);
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
	 * Removes a public key from the list of known keys.
	 * 
	 * @param 	host The host the public key was for.
	 * @param	pubKey The public key to remove.
	 */
	public void removePubKey(Byte host, String pubKey) {
		pubKeys.remove(host);
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
