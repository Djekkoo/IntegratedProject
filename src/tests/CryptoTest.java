package tests;

import main.EncryptionHandler;

public class CryptoTest extends TestCase {

	@Override
	protected int runTest() {
		// TODO Auto-generated method stub
		EncryptionHandler EH1 = new EncryptionHandler();
		EncryptionHandler EH2 = new EncryptionHandler();
		
		EH2.addPubKey((byte)1, EH1.getPubKey());
		EH1.addPubKey((byte)2, EH2.getPubKey());
		String testmessage = "Hello World!";
		byte[] encrypted = EH1.encrypt(testmessage.getBytes(), (byte)2);
		byte[] decrypted = EH2.decrypt(encrypted);
		assertEquals("Decrypted message", testmessage, new String(decrypted));
		return 0;
	}

	@Override
	protected void setUp() {
		// TODO Auto-generated method stub
		

	}

}
