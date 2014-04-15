package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;

public class EncryptionHandler {

	public static void main(String[] args) {
		EncryptionHandler EH = new EncryptionHandler();
	}
	
	public EncryptionHandler() {
		File privateKeyFile = new File("/home/joeyjo0/adhoc.pem");
		File publicKeyFile = new File("/home/joeyjo0/adhoc.pub");
		if(!privateKeyFile.exists() || !publicKeyFile.exists()) {
			try {
				RandomAccessFile privateKeyRAF = new RandomAccessFile(privateKeyFile, "rw");
				RandomAccessFile publicKeyRAF = new RandomAccessFile(publicKeyFile, "rw");
				
				KeyPair kp = generateKeyPair();
				
				privateKeyRAF.setLength(0);
				privateKeyRAF.write(DatatypeConverter.printBase64Binary(kp.getPrivate().getEncoded()).getBytes());
				
				publicKeyRAF.setLength(0);
				publicKeyRAF.write(DatatypeConverter.printBase64Binary(kp.getPublic().getEncoded()).getBytes());
				System.out.println("Keys generated");
				//DatatypeConverter
			} catch (IOException | NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(512);
        KeyPair keys = keyGen.genKeyPair();
        return keys;
	}
}
