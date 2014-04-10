package tests;

import networking.DataPacket;
import networking.DatagramDataSizeException;
import main.Callback;
import monitoring.NetworkMessage;
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
    
    public void empty(String text,Byte ding) {
    	System.out.println(text);
    }

    public int runTest() {
        setUp();
        //testChat();
        //testUsers();
        testEvents();
        //client.getGUI().dispose(); //TODO Terugzetten
        return errors;
    }

    protected void testChat() {
        startTest("Chat test");
        client.packetReceived(parseDatapacket("CHAT Joey: fuk joe",(byte) 0x0E));
        assertEquals("Aantal berichten", 2, client.getGUI().getChat().length);
        try { Thread.sleep(1000);} catch (InterruptedException e) {	}
        client.packetReceived(parseDatapacket("CHAT Jacco da man: fuk joe",(byte) 0x0E));
        try { Thread.sleep(1000);} catch (InterruptedException e) {	}
        client.packetReceived(parseDatapacket("CHAT Niggah: wat een gechat hier",(byte) 0x0E));
        assertEquals("Aantal berichten", 4, client.getGUI().getChat().length);
    }
    
    protected void testUsers() {
    	startTest("User test");
    	client.packetReceived(parseDatapacket("USER Joey",(byte) 0x0A));
    	assertEquals("Players", " Joey Neger", client.getGUI().getUsers());
    	try { Thread.sleep(1000);} catch (InterruptedException e) {	}
    	client.packetReceived(parseDatapacket("USER FLo",(byte) 0x0B));
    	assertEquals("Players", " FLo Joey", client.getGUI().getUsers());
    }
    
    protected void testEvents() {
    	startTest("Event test");
    	//simuleer dat Jacco joined en weer weggaat
    	client.updateNetwork((Byte) Byte.valueOf((byte) 0x0E),NetworkMessage.JOINED);
    	try { Thread.sleep(3000);} catch (InterruptedException e) {	} //de 3 sec delay
    	client.packetReceived(parseDatapacket("USER Jacco",(byte) 0x0E));
    	try { Thread.sleep(2000);} catch (InterruptedException e) {	} 
    	client.packetReceived(parseDatapacket("CHAT Jacco: got sum XTC up my arse today.",(byte) 0x0E));
    	
    	client.updateNetwork((Byte) Byte.valueOf((byte) 0x0D),NetworkMessage.JOINED);
    	try { Thread.sleep(3000);} catch (InterruptedException e) {	} //de 3 sec delay
    	client.packetReceived(parseDatapacket("USER Sander",(byte) 0x0D));
    	try { Thread.sleep(2000);} catch (InterruptedException e) {	} 
    	client.packetReceived(parseDatapacket("CHAT Sander: I LIKE TO SUK DIK",(byte) 0x0D));
    	try { Thread.sleep(5000);} catch (InterruptedException e) {	}
    	client.updateNetwork((Byte) Byte.valueOf((byte) 0x0D),NetworkMessage.DROPPED);
    	try { Thread.sleep(1000);} catch (InterruptedException e) {	}
    	
    	client.packetReceived(parseDatapacket("CHAT Jacco: got to get sum moar druks.",(byte) 0x0E));
    	try { Thread.sleep(2000);} catch (InterruptedException e) {	}
    	client.updateNetwork((Byte) Byte.valueOf((byte) 0x0E),NetworkMessage.DROPPED);
    	try { Thread.sleep(1000);} catch (InterruptedException e) {	}
    	client.tell("Test completed, fuck, you!");
    }
    
    protected void pvtChat() {
    	startTest("Private test");
    	client.updateNetwork((Byte) Byte.valueOf((byte) 0x0D),NetworkMessage.JOINED);
    	try { Thread.sleep(3000);} catch (InterruptedException e) {	} //de 3 sec delay
    	client.packetReceived(parseDatapacket("USER Sander",(byte) 0x0D));
    	try { Thread.sleep(2000);} catch (InterruptedException e) {	} 
    	client.packetReceived(parseDatapacket("CHAT Sander: I LIEK TO SUK DIK",(byte) 0x0D));
    	try { Thread.sleep(5000);} catch (InterruptedException e) {	}
    	client.updateNetwork((Byte) Byte.valueOf((byte) 0x0D),NetworkMessage.DROPPED);
    	try { Thread.sleep(1000);} catch (InterruptedException e) {	}
    }
    
    protected DataPacket parseDatapacket(String data, byte src) {
    	DataPacket packet = null;
    	 byte[] b = data.getBytes();
         //byte[] b = data.getBytes(Charset.forName("UTF-8"));
         try {
        	 packet = new DataPacket((byte) src, (byte) 0xf, (byte) 0xf, (byte) 0xf, b, (Boolean) true, (Boolean) true, (Boolean) true, (Boolean) true);
		} catch (DatagramDataSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return packet;
    }
}
