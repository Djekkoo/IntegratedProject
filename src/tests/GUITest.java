package tests;

import main.Communication;
import application.Client;
import application.GUI;

/** 
 * Testklasse-interface voor <code>application.GUI</code>.
 * @author FlorianF
 */
public class GUITest extends TestCase {
	GUI gui;
	
    protected void setUp() {
        gui = new Client(new Communication()).getGUI();
    }

    public int runTest() {
        setUp();
        testChat();
        testUsers();
        return fouten;
    }

    protected void testChat() {
        beginTest("Chat test");
        gui.updateChat("test");
        assertEquals("Aantal berichten", 2, gui.getChat().length);
        try { Thread.sleep(1000);} catch (InterruptedException e) {	}
        gui.updateChat("hoi Jacco");
        try { Thread.sleep(1000);} catch (InterruptedException e) {	}
        gui.updateChat("allemaal gechat");
        assertEquals("Aantal berichten", 4, gui.getChat().length);
    }
    
    protected void testUsers() {
    	beginTest("User test");
    	gui.updateUserlist(new String[]{"Joey","Neger"});
    	assertEquals("Players", " Joey Neger", gui.getUsers());
    	try { Thread.sleep(1000);} catch (InterruptedException e) {	}
    	gui.updateUserlist(new String[]{"Joey","Neger","Jan Boerman","Zoe Haas","Koekoekjongen"});
    	assertEquals("Players", " Joey Neger Jan Boerman Zoe Haas Koekoekjongen", gui.getUsers());
    }
}
