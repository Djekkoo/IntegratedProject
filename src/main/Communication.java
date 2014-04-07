package main;

import routing.LinkStateRouting;
import networking.Networker;

public class Communication {

	Networker network;
	LinkStateRouting router;
	
	public Communication() {
		
		network = new networking.Networker();
		router = new routing.LinkStateRouting();
		
		router.initialize(network);
		
	}
	
}
