package monitoring;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	private Callback networkCommuncation;
	
	private static long broadcastDelay = 500;
	private static long dumpDelay = 1500;
	private Map<Byte, Long> activity = new HashMap<Byte, Long>();

	
	public NetworkMonitor(Callback send, Callback error){
		this.send = send;
		this.networkCommuncation = error;
	}
	
	public void run() {
		
		Long time = System.currentTimeMillis();
		Long sleep = System.currentTimeMillis() - (time+broadcastDelay);
		Long threshold;
		Set<Byte> keys;
		Iterator<Byte> i;
		Byte key;
		
		while (true) {
			
			// broadcast
			try {
				//TODO: Change to broadcast
				this.send.invoke();
				time = System.currentTimeMillis();
			} catch (CallbackException e) {
				System.out.println(e.getLocalizedMessage());
			}
			
			// check for timeouts
			keys = this.activity.keySet();
			i    = keys.iterator();
			threshold = System.currentTimeMillis() - dumpDelay;
			while(i.hasNext()) {
				key = i.next();
				
				// Keep-alive not received
				if (this.activity.get(key) <= threshold) {
					this.activity.remove(key);
					try {
						this.networkCommuncation.invoke(key, NetworkMessage.NOKEEPALIVE);
					} catch (CallbackException e) { }
				}
				
			}
			
			// sleep
			try {
				
				sleep = System.currentTimeMillis() - (time+broadcastDelay);
				if (sleep > 50)
					Thread.sleep(sleep);
				
			} catch (InterruptedException e) {}
			
		}
		
	}
	
	// Received a new notification, putting heads-up at the data
	public void messageReceived(DataPacket p) {
		Byte source = p.getSource();
		
		if (!this.activity.containsKey(source)) {
			this.activity.put(source, System.currentTimeMillis());
			try {
				this.networkCommuncation.invoke(source, NetworkMessage.NEWKEEPALIVE);
			} catch (CallbackException e) {
				System.out.println(e.getLocalizedMessage());
			}
			return;
		}
		
		this.activity.put(source, System.currentTimeMillis());
		
	}
}
