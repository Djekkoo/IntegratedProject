package application;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import networking.DataPacket;
import main.Callback;
import main.CallbackException;
import main.IntegrationProject;
import monitoring.NetworkMessage;

//The door between GUI and Communication classes <3

/** 
 * @author      Florian Fikkert <f.a.j.fikkert@student.utwente.nl>
 * @version     0.1
 * @since       2014-04-07
 */
public class Client {
	private Callback sendMsg;
	private GUI gui;
	private String name;
	private Map<String,Byte> table;
	
	public Client(Callback sendMsg) {
		this.sendMsg = sendMsg;
		table = new HashMap<String,Byte>();
		//Start de GUI
		this.name = "";
		gui = new GUI(this);
		sendIdentity();
	}
	
	public GUI getGUI() {
		return gui;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
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
				String msg = splitdata[1] + ""; //vroeger ": "
				for(int i = 2; i < splitdata.length; i++) {
					msg = msg + " " + splitdata[i];
				}
				gui.updateChat(msg);
				break;
			case "USER":
				String usermsg = "";
				for(int i = 1; i < splitdata.length; i++) {
					usermsg = usermsg + splitdata[i];
				}
				table.put(usermsg,packet.getSource());
				updateUsers();
				break;
			default:
				System.out.println("The received command does not exist.");
				break;
			}
		}
	}

	public void sendChat(String text) {
		if (text.split(" ")[0].equals("!pvt")) {
			//DIRECT SEND
			Byte dest = table.get(text.split(" ")[1]);
			
			String chat = getName() + "(pvt): " + text;
			gui.updateChat(chat);
			try {
				sendMsg.invoke("CHAT " + chat,dest);
			} catch (CallbackException e) {
				System.out.println(e.getMessage());
			}
		} else {
			//BROADCAST
			String chat = getName() + ": " + text;
			gui.updateChat(chat);
			try {
				sendMsg.invoke("CHAT " + chat,Byte.valueOf((byte) 0x0F));
			} catch (CallbackException e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	public void sendIdentity() {
		table.put(name,IntegrationProject.DEVICE);
		try {
			sendMsg.invoke("USER " + name,Byte.valueOf((byte) 0x0F));
		} catch (CallbackException e) {
			System.out.println(e.getMessage());
		}
	}

	public void updateUsers() {
		String[] lijstje = new String[table.size()];
		int count = 0;
		for (Entry<String, Byte> entry : table.entrySet()) {
			lijstje[count] = entry.getKey();
			count++;
		    //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
		}
		gui.updateUserlist(lijstje);
	}
	
	public void updateNetwork(Byte source, NetworkMessage type) {
		
	}
	
}
