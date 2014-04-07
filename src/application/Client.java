package application;
import main.Callback;
import main.CallbackException;

//The door between GUI and Communication classes <3

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

	public void sendChat(String text) {
		//TODO: Fix de naam erbij
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
