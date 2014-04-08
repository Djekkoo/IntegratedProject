package monitoring;

import java.util.HashMap;
import java.util.Map;

import networking.DataPacket;
import main.Callback;
import main.CallbackException;

/**
 * Used for monitoring the network, communicates with RoutingInterface.
 * 
 * @author      Joey Haas <j.haas@student.utwente.nl>, Jacco Brandt <jacco@yayscripting.nl>
 * @version     0.1
 * @since       2014-04-07
 */
public class NetworkMonitor extends Thread {
	
	private Callback send;
	private Callback error;
	
	private static long broadcastDelay;
	private Map<Byte, Integer> activity = new HashMap<Byte, Integer>();

	
	public NetworkMonitor(Callback send, Callback error){
		this.send = send;
		this.error = error;
	}
	
	public void run() {
		
		while (true) {
			
			try {
				//TODO: Change to broadcast
				this.send.invoke();
			} catch (CallbackException e) {
				System.out.println(e.getLocalizedMessage());
			}
			
			try {
				Thread.sleep(broadcastDelay);
			} catch (InterruptedException e) {}
		}
		
	}
	
	public void messageReceived(DataPacket p) {
		byte source = p.getSource();
	}
}
