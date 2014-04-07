package main;

import routing.RoutingInterface;
import networking.DataPacket;
import networking.Networker;

public class Communication {

	Networker network;
	RoutingInterface router;
	
	public Communication() {
		
		network = new networking.Networker();
		router = new routing.LinkStateRouting(new Callback(this, "routerPolling"));
		
		router.initialize(network);
		
	}
	
	public DataPacket routerPolling() {
		
		
		return null;
		
	}
	
}
