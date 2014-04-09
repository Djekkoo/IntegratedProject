package monitoring;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
	
	private Callback broadcast;
	private Callback networkCommuncation;
	private Callback client;
	
	private static long broadcastDelay = 500;
	private static long dumpDelay = 1500;
	private Map<Byte, Long> activity = new HashMap<Byte, Long>();
	
	private Lock lock = new ReentrantLock();
	

	
	public NetworkMonitor(Callback broadcast, Callback error, Callback client){
		this.broadcast = broadcast;
		this.networkCommuncation = error;
		this.client = client;
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
				
				time = System.currentTimeMillis();
				// -> networking.Networker.broadcast();
				this.broadcast.invoke(new byte[0], Byte.valueOf((byte) 0), Boolean.FALSE, Boolean.FALSE, Boolean.TRUE);
				
			} catch (CallbackException e) {
				System.out.println(e.getLocalizedMessage());
			}
			
			this.lock.lock();
			
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
						this.client.invoke(key, NetworkMessage.NOKEEPALIVE);
						this.networkCommuncation.invoke(key, NetworkMessage.NOKEEPALIVE);
					} catch (CallbackException e) { }
				}
				
			}
			
			this.lock.unlock();
			
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
		this.lock.lock();
		
		if (!this.activity.containsKey(source)) {
			this.activity.put(source, System.currentTimeMillis());
			try {
				this.client.invoke(source, NetworkMessage.NEWKEEPALIVE);
				this.networkCommuncation.invoke(source, NetworkMessage.NEWKEEPALIVE);
			} catch (CallbackException e) {
				System.out.println(e.getLocalizedMessage());
			}
			return;
		}
		
		this.activity.put(source, System.currentTimeMillis());
		this.lock.unlock();
		
	}
}
