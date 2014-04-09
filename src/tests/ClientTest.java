package tests;

import networking.DataPacket;
import networking.DatagramDataSizeException;
import main.Callback;
import application.Client;

/** 
 * Testklasse-interface voor <code>application.GUI</code>.
 *
 * @author      Florian Fikkert <f.a.j.fikkert@student.utwente.nl>
 * @version     0.1
 * @since       2014-04-07
 */
public class ClientTest extends TestCase {
	Client client;
	
    protected void setUp() {
    	client = new Client(new Callback(this,"empty"));
    }
    
    public void empty(String text) {
    	System.out.println(text);
    }

    public int runTest() {
        setUp();
        testChat();
        testUsers();
        //client.getGUI().dispose(); //TODO Terugzetten
        return errors;
    }

    protected void testChat() {
        startTest("Chat test");
        client.packetReceived(parseDatapacket("CHAT Joey: fuk joe"));
        assertEquals("Aantal berichten", 2, client.getGUI().getChat().length);
        try { Thread.sleep(1000);} catch (InterruptedException e) {	}
        client.packetReceived(parseDatapacket("CHAT Jacco da man: fuk joe"));
        try { Thread.sleep(1000);} catch (InterruptedException e) {	}
        client.packetReceived(parseDatapacket("CHAT Niggah: wat een gechat hier"));
        assertEquals("Aantal berichten", 4, client.getGUI().getChat().length);
    }
    
    protected void testUsers() {
    	startTest("User test");
    	client.packetReceived(parseDatapacket("USER Joey"));
    	assertEquals("Players", " Joey Neger", client.getGUI().getUsers());
    	try { Thread.sleep(1000);} catch (InterruptedException e) {	}
    	client.packetReceived(parseDatapacket("USER FLo"));
    	assertEquals("Players", " FLo Joey", client.getGUI().getUsers());
    }
    
    protected DataPacket parseDatapacket(String data) {
    	DataPacket packet = null;
    	 byte[] b = data.getBytes();
         //byte[] b = data.getBytes(Charset.forName("UTF-8"));
         try {
        	 packet = new DataPacket((byte) 0xf, (byte) 0xf, (byte) 0xf, (byte) 0xf, b, (Boolean) true, (Boolean) true, (Boolean) true);
		} catch (DatagramDataSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return packet;
    }
}
