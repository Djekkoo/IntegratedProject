package main;

import java.io.IOException;
import java.net.SocketException;

import application.Client;
import routing.RoutingInterface;
import monitoring.NetworkMonitor;
import networking.DataPacket;
import networking.DatagramDataSizeException;
import networking.Networker;

/** 
 * @author      Jacco Brandt <j.h.brandt@student.utwente.nl>
 * @version     0.1
 * @since       2014-04-07
 */
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
//		new Thread(new Runnable() {
//		     public void run()
//		     {
//		          router.initialize();
//		     }
//		}).start();
		
		this.monitor = new NetworkMonitor(new Callback(network, "broadcast"), new Callback(router, "networkMessage"));
		this.monitor.start();
		
		this.client = new Client(new Callback(this, "sendMessage"));
		
	}
	
	public void newPacket(DataPacket packet) {
		
		System.out.println(packet.getSource() + "-" + new String(packet.getData()));
		
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
	
	public void sendMessage(String message, Byte destination) throws IOException, DatagramDataSizeException {
		
		if (destination.equals(0x0F)) {
			this.network.broadcast(message.getBytes(), this.router.getLongestRoute(), Boolean.FALSE, Boolean.FALSE, Boolean.TRUE);
			return;
		}
		
		this.network.send(destination, message.getBytes());
		
	}
	
}
