package tests;

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
        client.getGUI().dispose();
        return errors;
    }

    protected void testChat() {
        startTest("Chat test");
        client.receiveChat("test");
        assertEquals("Aantal berichten", 2, client.getGUI().getChat().length);
        try { Thread.sleep(1000);} catch (InterruptedException e) {	}
        client.receiveChat("hoi Jacco");
        try { Thread.sleep(1000);} catch (InterruptedException e) {	}
        client.receiveChat("allemaal gechat");
        assertEquals("Aantal berichten", 4, client.getGUI().getChat().length);
    }
    
    protected void testUsers() {
    	startTest("User test");
    	client.updateUsers("Joey;Neger");
    	assertEquals("Players", " Joey Neger", client.getGUI().getUsers());
    	try { Thread.sleep(1000);} catch (InterruptedException e) {	}
    	client.updateUsers("Joey;Neger;Jan Boerman;Zoe Haas;Koekoekjongen");
    	assertEquals("Players", " Joey Neger Jan Boerman Zoe Haas Koekoekjongen", client.getGUI().getUsers());
    }
}
