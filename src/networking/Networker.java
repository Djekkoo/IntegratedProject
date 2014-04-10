package networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Map.Entry;

import main.Callback;
import main.CallbackException;
import main.IntegrationProject;

/**
 * This class when instantiated handles the SKCP layer.
 * 
 * It supports broadcasting, sending receiving, and chopping into smaller
 * packets.
 * 
 * @author Sander Koning <s.koning@student.utwente.nl>
 * @version 0.1
 * @since 2014-04-07
 */
public class Networker {
	public static final int UNIPORT = 1337;
	public static final int MULTIPORT = 7001;

	public static InetAddress multicastAddress;
	
	Sequencer sequencer;

	DatagramSocket dSock;
	MulticastSocket mSock;

	Callback routerGetRoute;
	Callback packetReceived;

	public Networker(Callback routerPacketReceived) throws IOException {
		sequencer = new Sequencer();
		multicastAddress = InetAddress.getByName(IntegrationProject.BROADCAST);
		dSock = new DatagramSocket(UNIPORT);
		mSock = new MulticastSocket(MULTIPORT);
		mSock.setLoopbackMode(true);
		mSock.joinGroup(multicastAddress);

		this.packetReceived = routerPacketReceived;

		(new UniMonitor(new Callback(this, "receive"), dSock)).start();

		(new MultiMonitor(new Callback(this, "receive"), mSock)).start();
	}

	public void broadcast(byte[] data, Byte hops, Boolean nonSequence,
			Boolean routing, Boolean keepalive) throws IOException,
			DatagramDataSizeException {

		DataPacket dp;

		if (nonSequence) {
			dp = new DataPacket(IntegrationProject.DEVICE, (byte) 0x0F, (byte) 0x0,
					(byte) 0x0, data, false, routing, keepalive, false);
			
			if(hops != 0)
				System.out.println("WARNING: nonsequence broadcast can only have 0 hops");
		} else {
			dp = new DataPacket(IntegrationProject.DEVICE, (byte) 0x0F, hops,
					sequencer.getBroadcast(), data, false, routing, keepalive, false);
		}

		mSock.send(new DatagramPacket(dp.getRaw(), dp.getRaw().length,
				multicastAddress, MULTIPORT));
	}

	public void broadcast(DataPacket dp) throws IOException {
		mSock.send(new DatagramPacket(dp.getRaw(), dp.getRaw().length,
				multicastAddress, MULTIPORT));
	}

	public void setRouter(Callback router) {
		this.routerGetRoute = router;
	}

	public void send(Byte destination, byte[] data) throws IOException {

		Entry<Byte, Byte> connection = null;

		try {
			Object temp = routerGetRoute.invoke(new Byte(destination));
			if (temp instanceof Entry)
				connection = (Entry<Byte, Byte>) temp;
		} catch (CallbackException e1) {
			System.out
					.println("Error finding route. Possibly no route to that host.");
			return; // Route not found
		}

		LinkedList<DataPacket> packets = processData(destination,
				connection.getValue(), data);

		for (DataPacket p : packets) {
			dSock.send(new DatagramPacket(p.getRaw(), p.getRaw().length,
					getFullAddress(connection.getKey()), UNIPORT));
		}
	}

	public void send(DataPacket dp) throws IOException {
		Entry<Byte, Byte> connection = null;

		try {
			Object temp = routerGetRoute.invoke(dp.getDestination());
			if (temp instanceof Entry)
				connection = (Entry<Byte, Byte>) temp;
		} catch (CallbackException e1) {
			System.out
					.println("Error finding route. Possibly no route to that host.");
			return; // Route not found
		}

		dSock.send(new DatagramPacket(dp.getRaw(), dp.getRaw().length,
				getFullAddress(connection.getKey()), UNIPORT));
	}

	private byte[] processPackets(LinkedList<DataPacket> packets) {

		int maxChunkSize = 1024 - DataPacket.HEADER_LENGTH;
		int length = (packets.size() - 1) * maxChunkSize
				+ packets.getLast().getData().length;

		byte[] result = new byte[length];
		byte[] buffer = new byte[0];

		int i = 0;
		while (!packets.isEmpty()) {
			buffer = packets.pop().getData();
			System.arraycopy(buffer, 0, result, i * maxChunkSize, buffer.length);
			i++;
		}

		return result;
	}

	private LinkedList<DataPacket> processData(Byte destination, Byte hops,
			byte[] data) {
		LinkedList<DataPacket> result = new LinkedList<DataPacket>();

		DataPacket dp;

		boolean moar = false;

		int maxChunkSize = 1024 - DataPacket.HEADER_LENGTH;
		byte[] chunk = new byte[maxChunkSize];

		for (int i = 0; i <= Math.ceil(data.length / maxChunkSize); i++) {
			if (data.length >= (i + 1) * maxChunkSize) {
				moar = true;
				System.arraycopy(data, i * maxChunkSize, chunk, 0, maxChunkSize);
			} else {
				moar = false;
				chunk = new byte[data.length - i * maxChunkSize];
				System.arraycopy(data, i * maxChunkSize, chunk, 0, data.length
						- i * maxChunkSize);
			}

			try {
				dp = new DataPacket(IntegrationProject.DEVICE, destination,
						hops, sequencer.getTo(destination), chunk, false, false, false,
						moar);
				result.add(dp);
			} catch (DatagramDataSizeException e) {
				e.printStackTrace();
			}
		}

		return result;

	}

	private InetAddress getFullAddress(Byte postfix) {

		byte[] address = new byte[] { (byte) 192, (byte) 168, (byte) 5, postfix };

		try {
			return InetAddress.getByAddress(address);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void receive(DataPacket d) throws IOException {
		// TODO Put packets together if necessary
		if (d.getDestination() == IntegrationProject.DEVICE
				|| d.getDestination() == (byte) 0x0F) {

			System.out.println("Received packet for me");

			try {
				packetReceived.invoke(d);
			} catch (CallbackException e) {
			}

			if (d.getDestination() == (byte) 0x0F && d.getHops() > 0
					&& d.getDestination() != IntegrationProject.DEVICE) {
				d.decreaseHops();
				broadcast(d);
			}
		} else {
			d.decreaseHops();
			if (d.getHops() > 0) {
				send(d);
			}
		}
	}
}
