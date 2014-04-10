package application;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javazoom.jl.decoder.JavaLayerException;

import com.gtranslate.Audio;
import com.gtranslate.Language;

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
	private Map<Byte,String> table;
	
	public Client(Callback sendMsg) {
		this.sendMsg = sendMsg;
		table = new HashMap<Byte,String>();
		//Start de GUI
		this.name = "";
		gui = new GUI(this);
		//wait for initiation sequence
		try {Thread.sleep(3000);} catch (Exception e) {	}
		sendIdentity();
	}
	
	public GUI getGUI() {
		return gui;
	}
	
	public void setName(String name) {
		this.name = name;
		//tell("Welcome "+name+"... Please, .. fuck, ... me.");
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
				//tell(msg);
				break;
			case "USER":
				String usermsg = "";
				for(int i = 1; i < splitdata.length; i++) {
					usermsg = usermsg + splitdata[i];
				}
				table.put(packet.getSource(),usermsg);
				System.out.println(usermsg + " detected.");
				//tell("Some bitch.. ass... nigger.. "+usermsg+"... joined.");
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
			Byte dest = 0x00;
			for (Entry<Byte, String> entry : table.entrySet()) {
				if (entry.getValue().equals(text.split(" ")[1])) {
					dest = entry.getKey();
				}
			    //tell("Key = " + entry.getKey() + ", Value = " + entry.getValue());
			}
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
		table.put(IntegrationProject.DEVICE,name);
		try {
			sendMsg.invoke("USER " + name,Byte.valueOf((byte) 0x0F));
		} catch (CallbackException e) {
			System.out.println(e.getMessage());
		}
		updateUsers();
	}

	public void updateUsers() {
		String[] lijstje = new String[table.size()];
		int count = 0;
		for (Entry<Byte, String> entry : table.entrySet()) {
			lijstje[count] = entry.getValue();
			count++;
		    //tell("Key = " + entry.getKey() + ", Value = " + entry.getValue());
		}
		gui.updateUserlist(lijstje);
	}
	
	public void updateNetwork(Byte source, NetworkMessage type) {
		if (type == NetworkMessage.DROPPED) {
			System.out.println("Our friend "+table.get(source)+" dropped it like it's hot.");
			table.remove(source);
			updateUsers();
		} else if (type == NetworkMessage.JOINED) {
			System.out.println("Someone from source "+source+" joined.");
			try {
				sendMsg.invoke("USER " + name,source);
			} catch (CallbackException e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	public void tell(String msg) {
		String text = msg.replace(":", " says, ..");
		System.out.println(text);
		if (1+1==3) {
			Audio audio = Audio.getInstance();
			InputStream sound;
			try {
				sound = audio.getAudio(text, Language.ENGLISH);
				try {
					audio.play(sound);
				} catch (JavaLayerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
