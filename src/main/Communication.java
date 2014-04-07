package main;

import java.net.SocketException;
import java.util.LinkedList;

import application.Client;
import routing.RoutingInterface;
import monitoring.NetworkMonitor;
import networking.DataPacket;
import networking.Networker;

public class Communication {

	Client client;
	Networker network;
	RoutingInterface router;
	NetworkMonitor monitor;
	LinkedList<DataPacket> routerQueue = new LinkedList<DataPacket>();
	
	public Communication() {
		
		try {
			network = new networking.Networker(new Callback(this, "newPacket"));
		} catch (SocketException e) {
			System.out.println("Something terrible happened, Sander screwed up his class: " + e.getMessage());
			System.exit(0);
		}
		
		router = new routing.LinkStateRouting(new Callback(this, "routerPolling"), new Callback(network, "send"));
		network.setRouter(new Callback(router, "getRoute"));
		new Thread(new Runnable() {
		     public void run()
		     {
		          router.initialize();
		     }
		}).start();
		
		monitor = new NetworkMonitor(new Callback(network, "send"), new Callback(router, "networkError"));
		
		
		this.client = new Client(new Callback(this, "sendMessage"));
		
	}
	
	public void newPacket(DataPacket packter) {
		
	}
	
	public void sendMessage(String message) {
		
	}
	
	public synchronized DataPacket routerPolling() {
		
		if (this.routerQueue.size() == 0)
			return null;
		
		return this.routerQueue.removeFirst();
		
	}
	
}
