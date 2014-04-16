package tests;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import main.CryptoException;
import main.EncryptionHandler;

public class CryptoTest extends TestCase {

	@Override
	protected int runTest() {
		// TODO Auto-generated method stub
		byte[] packet;
		String testmessage = "Hello World!";
		
		EncryptionHandler EH1 = new EncryptionHandler();
		EncryptionHandler EH2 = new EncryptionHandler();
		EncryptionHandler EH3 = new EncryptionHandler();
		
		EH2.addPubKey((byte)1, EH1.getPubKey());
		EH1.addPubKey((byte)2, EH2.getPubKey());
		
		packet = EH3.getPubKeyPacket();
		EH2.addPubKey((byte)3, EncryptionHandler.parsePubKeyPacket(packet));
		
		System.out.println("Encrypt/Decrypt tests running");
		
		byte[] encrypted = EH1.encrypt(testmessage.getBytes(), (byte)2);
		byte[] decrypted = EH2.decrypt(encrypted);
		assertEquals("Decrypted message #1", testmessage, new String(decrypted));
		
		decrypted = EH3.decrypt(EH2.encrypt(testmessage.getBytes(), (byte)3));
		assertEquals("Decrypted message #2", testmessage, new String(decrypted));
		
		System.out.println("Sign/Validate tests running");
		byte[] signedHash = EH1.sign(testmessage.getBytes());
		boolean validated;
		//EH2 knows EH1s pubKey
		validated = EH2.validate((byte)1, testmessage.getBytes(), signedHash);
		assertEquals("Validating packet from 1 to 2", true, validated);
		
		//EH2 does NOT know EH1s pubKey
		validated = EH3.validate((byte)1, testmessage.getBytes(), signedHash);
		assertEquals("Validating packet from 1 to 3", false, validated);
		
		System.out.println("Big packet tests running");
		byte[] randomBytes = new byte[1024];
		try {
			RandomAccessFile urandom = new RandomAccessFile(new File("/dev/urandom"),"r");
			urandom.read(randomBytes);
			urandom.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		byte[] bigEncrypted = EH1.encrypt(randomBytes, (byte)2);
		byte[] bigDecrypted = EH2.decrypt(bigEncrypted);
		int i = 0;
		boolean correct = true;
		for(byte randomByte : randomBytes) {
			if(randomByte != bigDecrypted[i]) {
				System.out.println("Error: " + randomByte + " != " + bigDecrypted[i] + " at i=" + i);
				correct = false;
			}
			i++;
		}
		assertEquals("Big packet", true, correct);
		
		System.out.println("Combined tests running");
		
		EH1.addPubKey((byte)3, EH3.getPubKey());
		byte[] encFor2 = EH1.getPacket(randomBytes, (byte)2);
		byte[] encFor3 = EH1.getPacket(randomBytes, (byte)3);
		
		try {
			byte[] decBy2 = EH2.parsePacket(encFor2, (byte)1);
			byte[] decBy3 = EH3.parsePacket(encFor3, (byte)1);
		} catch (CryptoException e) {
			e.printStackTrace();
		}
		
		return 0;
	}

	@Override
	protected void setUp() {
		// TODO Auto-generated method stub
		

	}

}
