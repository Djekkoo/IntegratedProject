package main;

import java.io.IOException;
import java.net.SocketException;

import application.Client;
import routing.RoutingInterface;
import monitoring.NetworkMessage;
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
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		router = new routing.LinkStateRouting(new Callback(network, "send"), new Callback(this, "updateNetwork"));
		network.setRouter(new Callback(router, "getRoute"));
//		new Thread(new Runnable() {
//		     public void run()
//		     {
//		          router.initialize();
//		     }
//		}).start();
		
		this.client = new Client(new Callback(this, "sendMessage"),router);
		
		this.monitor = new NetworkMonitor(this.router, new Callback(network, "broadcast"));
		this.monitor.start();
		
	}
	 
	public void updateNetwork(Byte source, NetworkMessage type) {
		if (type.equals(NetworkMessage.JOINED)) {
			this.network.handshake(source);
		}
		this.client.updateNetwork(source, type);
	}
	
	public void newPacket(DataPacket packet) {
		
		System.out.println(packet.getSource() + "-" + new String(packet.getData()));
		if (packet.isKeepAlive()) {
			this.monitor.messageReceived(packet);
		}
		else if (packet.isRouting()) {
			this.router.packetReceived(packet);
		}
		else {
			this.client.packetReceived(packet);
		}
		
	}
	
	public void sendMessage(String message, Byte destination) throws IOException, DatagramDataSizeException {
		
		if (destination.equals(Byte.valueOf((byte) 0x0F))) {
			
			for(Byte knownDestination : router.getDevices()){
				System.out.println("Know destination: " + knownDestination);
				if(knownDestination.byteValue() != IntegrationProject.DEVICE)
					this.network.send(knownDestination, message.getBytes());
			}
			
		} else {
			this.network.send(destination, message.getBytes());
		}
		
	}

	public void shutDown() {
		
		try {
			this.network.broadcast(new byte[0], this.router.getLongestRoute(), Boolean.FALSE, Boolean.TRUE, Boolean.TRUE);
		} catch(Exception e) {}
		
	}
	
}