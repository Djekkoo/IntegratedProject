package main;

import java.net.SocketException;
import java.util.LinkedList;

import routing.RoutingInterface;
import networking.DataPacket;
import networking.Networker;

public class Communication {

	Networker network;
	RoutingInterface router;
	LinkedList<DataPacket> routerQueue = new LinkedList<DataPacket>();
	
	public Communication() {
		
		try {
			network = new networking.Networker();
		} catch (SocketException e) {
			System.out.println("Something terrible happened, Sander screwed his class: " + e.getMessage());
			System.exit(0);
		}
		
		router = new routing.LinkStateRouting(new Callback(this, "routerPolling"), new Callback(network, "send"));
		router.initialize();
		
	}
	
	public DataPacket routerPolling() {
		
		if (this.routerQueue.size() == 0)
			return null;
		
		return this.routerQueue.removeFirst();
		
	}
	
}
