package monitoring;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import routing.RoutingInterface;
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
	private Callback newClient;
	
	private static long broadcastDelay = 500;
	private static long dumpDelay = 1500;
	private Map<Byte, Long> activity = new HashMap<Byte, Long>();
	
	private Lock lock = new ReentrantLock();
	private RoutingInterface router;
	

	
	public NetworkMonitor(RoutingInterface router, Callback broadcast, Callback client){
		this.router = router;
		this.broadcast = broadcast;
		this.newClient = client;
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
						this.newClient.invoke(key, NetworkMessage.NOKEEPALIVE);
						this.router.networkMessage(key, NetworkMessage.NOKEEPALIVE);
					} catch (CallbackException e) { }
				}
				
			}
			
			this.lock.unlock();
			
			// sleep
			try {
				
				sleep = System.currentTimeMillis() - time + broadcastDelay;
				if (sleep > 50)
					Thread.sleep(sleep);
				
			} catch (InterruptedException e) {}
			
		}
		
	}
	
	// Received a new notification, putting heads-up at the data
	public void messageReceived(DataPacket p) {
		
		if (!p.isKeepAlive()) {
			System.out.println("Error, packet delivered at network monitor is NOT a keep-alive packet!");
			return;
		}
		
		Byte source = p.getSource();
		this.lock.lock();
		
		// shutdown?
		if (p.isRouting()) {
			if (this.activity.containsKey(source)) {
				this.activity.remove(source);
				try {
					this.router.networkMessage(source, NetworkMessage.DROPPED);
					if (this.router.isReachable(source).equals(Boolean.FALSE))
						this.newClient.invoke(source, NetworkMessage.DROPPED);
				}
				catch (CallbackException e) {
					System.out.println(e.getLocalizedMessage());
				}
			}
			
			return;
		}
		
		// alive
		if (!this.activity.containsKey(source)) {
			this.activity.put(source, System.currentTimeMillis());
			try {
				
				if (this.router.isReachable(source).equals(Boolean.FALSE))
					this.newClient.invoke(source, NetworkMessage.JOINED);
				this.router.networkMessage(source, NetworkMessage.NEWKEEPALIVE);
				
			} catch (CallbackException e) {
				System.out.println(e.getLocalizedMessage());
			}
			return;
		}
		
		this.activity.put(source, System.currentTimeMillis());
		this.lock.unlock();
		
	}
}
