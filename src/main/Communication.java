package main;

import java.net.SocketException;

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
	
	public Communication() {
		
		try {
			network = new networking.Networker(new Callback(this, "newPacket"));
		} catch (SocketException e) {
			System.out.println("Something terrible happened, Sander screwed up his class: " + e.getMessage());
			System.exit(0);
		}
		
		router = new routing.LinkStateRouting(new Callback(network, "send"));
		network.setRouter(new Callback(router, "getRoute"));
		new Thread(new Runnable() {
		     public void run()
		     {
		          router.initialize();
		     }
		}).start();
		
		this.monitor = new NetworkMonitor(new Callback(network, "send"), new Callback(router, "networkMessage"));
		this.monitor.run();
		
		this.client = new Client(new Callback(this, "sendMessage"));
		
	}
	
	public void newPacket(DataPacket packet) {
		
		if (packet.isRouting()) {
			this.router.packetReceived(packet);
		}
		else if (packet.isKeepAlive()) {
			this.monitor.messageReceived(packet);
		}
		else {
			this.client.packetReceived(packet);
		}
		
	}
	
	public void sendMessage(String message) {
		
	}
	
}
