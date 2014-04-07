//The door between GUI and Communication classes <3

public class Client {
	private Communication communication;
	private GUI gui;
	
	public Client(Communication communication) {
		this.communication = communication;
		//Start de GUI
		gui = new GUI(this);
	}
	
	//Communicatie functies:	

	public void sendChat(String text) {
		//stuur naar communication
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
