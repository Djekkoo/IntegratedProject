package main;

import java.util.LinkedList;

import routing.RoutingInterface;
import networking.DataPacket;
import networking.Networker;

public class Communication {

	Networker network;
	RoutingInterface router;
	LinkedList<DataPacket> routerQueue = new LinkedList<DataPacket>();
	
	public Communication() {
		
		network = new networking.Networker();
		router = new routing.LinkStateRouting(new Callback(this, "routerPolling"));
		
		router.initialize(new Callback(network, "send"));
		
	}
	
	public DataPacket routerPolling() {
		
		if (this.routerQueue.size() == 0)
			return null;
		
		return this.routerQueue.removeFirst();
		
	}
	
}
