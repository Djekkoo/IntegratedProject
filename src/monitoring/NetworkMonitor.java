package monitoring;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import routing.RoutingInterface;
import networking.DataPacket;
import networking.SmallPacket;
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
	
	private static long broadcastDelay = 500;
	private static long dumpDelay = 1500;
	private Map<Byte, Long> activity = new HashMap<Byte, Long>();
	
	private Lock lock = new ReentrantLock();
	private RoutingInterface router;
	

	
	public NetworkMonitor(RoutingInterface router, Callback broadcast){
		this.router = router;
		this.broadcast = broadcast;
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
			
			List<Byte> l = new LinkedList<Byte>();
			
			while(i.hasNext()) {
				key = i.next();
				
				// Keep-alive not received
				if (this.activity.get(key) <= threshold) {
					l.add(key);
				}
				
			}
			
			for(Byte b : l){
				this.activity.remove(b);
				this.router.networkMessage(b, NetworkMessage.NOKEEPALIVE);
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
	public void messageReceived(DataPacket packet) {
		
		if (!packet.isKeepAlive()) {
			System.out.println("Error, packet delivered at network monitor is NOT a keep-alive packet!");
			return;
		}
		
		Byte source = packet.getSource();
		this.lock.lock();
		
		// shutdown?
		if (packet.isRouting()) {
			if (this.activity.containsKey(source)) {
				this.activity.remove(source);
				this.router.networkMessage(source, NetworkMessage.DROPPED);
			}
			
			this.lock.unlock();
			return;
		}
		
		// alive
		if (!this.activity.containsKey(source)) {
			this.activity.put(source, System.currentTimeMillis());
			this.router.networkMessage(source, NetworkMessage.NEWKEEPALIVE);
			
			this.lock.unlock();
			return;
		}
		
		this.activity.put(source, System.currentTimeMillis());
		this.lock.unlock();
		
	}
}
