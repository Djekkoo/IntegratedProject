package application;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import routing.LinkStateRouting;
import routing.RoutingInterface;
import networking.DataPacket;
import networking.SmallPacket;
import main.Callback;
import main.CallbackException;
import main.FileTransferHandler;
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
	private String filename = "file";
	private Map<Byte,String> table;
	private RoutingInterface router;
	private boolean hardcoremode;
	
	public Client(Callback sendMsg, RoutingInterface router) {
		hardcoremode = true;
		this.sendMsg = sendMsg;
		this.router = router;
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
		data = new String(packet.getData());
		
		if (!data.equals("") && data.split(" ").length > 1) {
			String[] splitdata = data.split(" ");
			switch(splitdata[0]) {
			case "CHAT":
				//Check for commands
				if (data.contains("/send")) {
					this.filename = gui.saveDialog(data.split("/send ")[1]);
					break;
				}
				if (data.contains("/Joey")) {
					System.out.println("ERROR IN LINKSTATE ROUTING!!!1!! \n Joeyiseenechtvirus.jpg.exe not found!");
					System.exit(0);
					break;
				}
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
				table.put(packet.getSource(),usermsg);
				System.out.println(usermsg + " detected.");
				updateUsers();
				break;
			case "FILE":
				System.out.println("FILE INCOMING");
				try {
					FileTransferHandler fth = new FileTransferHandler();
					byte[] filedata = new byte[packet.getData().length-5];
					System.arraycopy(packet.getData(), 5, filedata, 0, filedata.length);
					byte[] file = fth.parsePacket(filedata);
					// TODO Save shit
					String filename = this.filename;
					fth.setFile(filename, "rw");
					fth.writeFile(file);
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println("FILE WRITED");
				break;
			default:
				System.out.println("The received command does not exist.");
				break;
			}
		}
	}

	public void sendChat(String text) {
		if (!text.equals("")) {
			if (text.split(" ")[0].contains("/")) {
				switch (text.split(" ")[0]) {
				case "/pvt":
					//DIRECT SEND
					Byte dest = 0x00;
					for (Entry<Byte, String> entry : table.entrySet()) {
						if (entry.getValue().contains(text.split("/pvt ")[1].split(":")[0])) {
							dest = entry.getKey();
						}
					}
					//parse
					String chat = "(PM "+getName() + "): " + text.split(":")[1];
					gui.updateChat("Private Message send to "+ text.split(":")[0]);

					try {
						sendMsg.invoke("CHAT " + chat,dest);
					} catch (CallbackException e) {
						System.out.println(e.getMessage());
					}
					break;
				case "/send":
					String sendje = getName() + ": " + text;
					gui.updateChat(sendje);
					try {
						sendMsg.invoke("CHAT " + sendje,Byte.valueOf((byte) 0x0F));
					} catch (CallbackException e) {
						System.out.println(e.getMessage());
					}
					break;
				case "/shownetwork":
					gui.updateChat("Showing network in the console");
					((LinkStateRouting) router).showNetwork();
					break;
				case "/users":
					gui.updateChat("Showing the user table in Client:");
					for (Entry<Byte, String> entry : table.entrySet()) {
						gui.updateChat("Source = " + entry.getKey() + ", Name = " + entry.getValue());
					}
					break;
				case "/showkeys":
					gui.updateChat("JoeyIsEchtGeenVirus.jpg.exe not found error");
					break;
				case "/hardcore":
					if (hardcoremode) {
						hardcoremode = false;
					} else {
						hardcoremode = true;
					}
					break;
				}
				} else {
				String chat = getName() + ": " + text;
				gui.updateChat(chat);

				try {
					sendMsg.invoke("CHAT " + chat,Byte.valueOf((byte) 0x0F));
				} catch (CallbackException e) {
					System.out.println(e.getMessage());
				}
			}
		}
	}
	
	//Always to everyone (BROADCAST)
	public void sendIdentity() {
		table.put(IntegrationProject.DEVICE,name); // YOURSELF
		try {	sendMsg.invoke("USER " + name,Byte.valueOf((byte) 0x0F));} catch (CallbackException e) { System.out.println(e.getMessage());}
		updateUsers();
	}
	
	public void sendFile(String filename) {
		try {
			FileTransferHandler fthr = new FileTransferHandler(filename,"r");
			sendMsg.invoke(fthr.getPacket(),Byte.valueOf((byte) 0x0F));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	

	public void updateUsers() {
		String[] lijstje = new String[table.size()];
		int count = 0;
		for (Entry<Byte, String> entry : table.entrySet()) {
			lijstje[count] = entry.getValue();
			count++;
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
			updateUsers();
			if (hardcoremode) {
				try {
					sendMsg.invoke("USER " + name,Byte.valueOf((byte) 0x0F));
				} catch (CallbackException e) {				}
			} else {
				try {
					sendMsg.invoke("USER " + name,source);
				} catch (CallbackException e) {				}
			}
		}
	}
}
