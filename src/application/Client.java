package application;
import java.io.UnsupportedEncodingException;

import networking.DataPacket;
import main.Callback;
import main.CallbackException;

//The door between GUI and Communication classes <3

/** 
 * @author      Florian Fikkert <f.a.j.fikkert@student.utwente.nl>
 * @version     0.1
 * @since       2014-04-07
 */
public class Client {
	private Callback sendMsg;
	private GUI gui;
	
	public Client(Callback sendMsg) {
		this.sendMsg = sendMsg;
		//Start de GUI
		gui = new GUI(this);
	}
	
	public GUI getGUI() {
		return gui;
	}
	
	//Communicatie functies:	
	/**
	 * Packet data format:
	 * CHAT Name Message
	 * USER Name;Name;Name;..
	 */

	public void packetReceived(DataPacket packet) {
		String data = "";
		try {
			data = new String(packet.getData(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			System.out.println(e.getMessage());
		}
		
		if (!data.equals("") && data.split(" ").length > 1) {
			String[] splitdata = data.split(" ");
			switch(splitdata[0]) {
			case "CHAT":
				String msg = splitdata[1] + ": ";
				for(int i = 2; i < splitdata.length; i++) {
					msg = msg + " " + splitdata[i];
				}
				this.receiveChat(msg);
				break;
			case "USER":
				
				break;
			default:
				System.out.println("The received command does not exist.");
				break;
			}
		}
	}

	public void sendChat(String text) {
		//TODO: Stuur naar andere peers
		gui.updateChat(text);
		try {
			sendMsg.invoke(text);
		} catch (CallbackException e) {
			System.out.println(e.getMessage());
		}
	}

	public void receiveChat(String msg) {
		gui.updateChat(msg);
	}

	// Uitgaande van een string die de users splitst met ; zonder verdere spaties etc.
	public void updateUsers(String users) {
		String[] lijstje = new String[users.split(";").length];
		for (int i = 0; i < users.split(";").length; i++) {
			if (users.split(";")[i] != null) {
				lijstje[i] = users.split(";")[i];
			}
		}
		gui.updateUserlist(lijstje);
	}
	
}
