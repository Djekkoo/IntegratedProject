package routing;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Used to retransmit the routing table every couple of seconds.
 * 
 * @author      Joey Haas <j.haas@student.utwente.nl>, Jacco Brandt <jacco@yayscripting.nl>
 * @version     0.1
 * @since       2014-04-16
 */
public class RoutingRepeater extends Thread {

	private LinkStateRouting router;
	private static long broadcastDelay = 1000;
	
	public RoutingRepeater(LinkStateRouting r, long delay) {
		this.router = r;
		this.broadcastDelay = delay;
	}
	
	public void run() {
		Thread.currentThread().setName("RouterSpamThread");
		
		while (true) {
			this.router.transmit();
			
			// sleep
			try {
				Thread.sleep(broadcastDelay);
				
			} catch (InterruptedException e) {}
		}
		
	}
}
