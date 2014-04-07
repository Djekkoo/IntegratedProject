//The door between GUI and Communication classes <3

public class Client {
	private Communication communication;
	private GUI gui;
	
	public Client(Communication communication) {
		this.communication = communication;
		
		//Start de GUI
		gui = new GUI(this);
	}
}
