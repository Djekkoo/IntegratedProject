package tests;

import main.Callback;
import application.Client;
import application.GUI;

/** 
 * Testklasse-interface voor <code>application.GUI</code>.
 *
 * @author      Florian Fikkert <f.a.j.fikkert@student.utwente.nl>
 * @version     0.1
 * @since       2014-04-07
 */
public class GUITest extends TestCase {
	GUI gui;
	
    protected void setUp() {
        gui = new Client(new Callback(this,"empty")).getGUI();
    }
    
    public void empty(String text,Byte ding) {
    	System.out.println(text);
    }

    public int runTest() {
        setUp();
        testChat();
        testUsers();
        gui.dispose();
        return errors;
    }

    protected void testChat() {
        startTest("Chat test");
        gui.updateChat("test");
        assertEquals("Aantal berichten", 2, gui.getChat().length);
        try { Thread.sleep(1000);} catch (InterruptedException e) {	}
        gui.updateChat("hoi Jacco");
        try { Thread.sleep(1000);} catch (InterruptedException e) {	}
        gui.updateChat("allemaal gechat");
        assertEquals("Aantal berichten", 4, gui.getChat().length);
    }
    
    protected void testUsers() {
    	startTest("User test");
    	gui.updateUserlist(new String[]{"Joey","Neger"});
    	assertEquals("Players", " Joey Neger", gui.getUsers());
    	try { Thread.sleep(1000);} catch (InterruptedException e) {	}
    	gui.updateUserlist(new String[]{"Joey","Neger","Jan Boerman","Zoe Haas","Koekoekjongen"});
    	assertEquals("Players", " Joey Neger Jan Boerman Zoe Haas Koekoekjongen", gui.getUsers());
    }
}
