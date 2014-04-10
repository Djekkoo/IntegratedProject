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
	
	/**
	 * 
	 * @param routerPacketReceived callback to function that handles incoming packets
	 * @throws IOException
	 */

	public Networker(Callback routerPacketReceived) throws IOException {
		routerGetRoute = new Callback(this, "dummyRoute");
		
		sequencer = new Sequencer(new Callback(this, "resend"));
		multicastAddress = InetAddress.getByName(IntegrationProject.BROADCAST);
		dSock = new DatagramSocket(UNIPORT);
		mSock = new MulticastSocket(MULTIPORT);
		mSock.setLoopbackMode(true);
		mSock.joinGroup(multicastAddress);

		this.packetReceived = routerPacketReceived;

		(new UniMonitor(new Callback(this, "receive"), dSock)).start();

		(new MultiMonitor(new Callback(this, "receive"), mSock)).start();
	}

	/**
	 * Forms new DataPackets and broadcasts them
	 * 
	 * @param data Data to be sent
	 * @param hops Number of hops it can traverse
	 * @param nonSequence Used if it should skip the sequencer
	 * @param routing Is used by routing protocol
	 * @param keepalive Is used as a keep alive signal
	 * @throws IOException When the socket can be reached
	 */
	public void broadcast(byte[] data, Byte hops, Boolean nonSequence,
			Boolean routing, Boolean keepalive) throws IOException{

		if (nonSequence) {
			try {
				DataPacket dp = new DataPacket(IntegrationProject.DEVICE, (byte) 0x0F,
						(byte) 0x0, (byte) 0x0, data, false, routing, keepalive,
						false);
				mSock.send(new DatagramPacket(dp.getRaw(), dp.getRaw().length,
						multicastAddress, MULTIPORT));
			} catch (DatagramDataSizeException e) {
				e.printStackTrace();
			}

			if (hops != 0)
				System.out
						.println("WARNING: nonsequence broadcast can only have 0 hops");
		} else {
			
			LinkedList<DataPacket> packets = processData((byte) 0x0F,
					hops, data, false, routing, keepalive);

			for (DataPacket p : packets) {
				mSock.send(new DatagramPacket(p.getRaw(), p.getRaw().length,
						multicastAddress, MULTIPORT));
			}
			
		}
	}
	
	/**
	 * Broadcasts a packet that is already formed
	 * 
	 * @param dp Preformed DataPacket
	 * @throws IOException When the socket can be reached
	 */

	public void broadcast(DataPacket dp) throws IOException {
		mSock.send(new DatagramPacket(dp.getRaw(), dp.getRaw().length,
				multicastAddress, MULTIPORT));
	}

	/**
	 * Sets the callback for retrieving a route to a node
	 * 
	 * @param router Callback to the function of Routing
	 */
	public void setRouter(Callback router) {
		this.routerGetRoute = router;
	}

	/**
	 * Sends data to a specific host
	 * 
	 * @param destination The end node it should be passed on to
	 * @param data The data to be sent
	 * @throws IOException When the socket can be reached
	 */
	@SuppressWarnings("unchecked")
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
				connection.getValue(), data, false, false, false);

		for (DataPacket p : packets) {
			dSock.send(new DatagramPacket(p.getRaw(), p.getRaw().length,
					getFullAddress(connection.getKey()), UNIPORT));
		}
	}

	/**
	 * Sends a preformed DataPacket
	 * 
	 * @param dp The DataPacket it should send
	 * @throws IOException When the socket can be reached
	 */
	@SuppressWarnings("unchecked")
	public void send(DataPacket dp) throws IOException, BigPacketSentException {
		if (dp instanceof BigPacket)
			throw new BigPacketSentException();

		Entry<Byte, Byte> connection;
		try {
			Object temp = routerGetRoute.invoke(dp.getDestination());
			if (temp instanceof Entry)
				connection = (Entry<Byte, Byte>) temp;
			else
				throw new NullPointerException();
		} catch (NullPointerException | CallbackException e1) {
			System.out
					.println("Error finding route. Possibly no route to that host.");
			return;
		}

		dSock.send(new DatagramPacket(dp.getRaw(), dp.getRaw().length,
				getFullAddress(connection.getKey()), UNIPORT));
	}
	/**
	 * When one of the monitors receives a DataPacket it should pass it on to here
	 * 
	 * @param d The received DataPacket
	 * @throws IOException When the socket can be reached
	 */
	
	@SuppressWarnings("unchecked")
	public void receive(DataPacket d) throws IOException {
		if (d.getDestination() == (byte) 0x0F) {
			if (d.getSequenceNumber() == 0) { // Reserved for packets that don't use sequencenumbers
				try {
					packetReceived.invoke(d);
				} catch (CallbackException e) {
				}
			} else {
				try {
					offer(d);
				} catch (CallbackException e) {
				}
			}

			if (d.getHops() > 0) {
				d.decreaseHops();
				broadcast(d);
			}
		} else if (d.getDestination() == IntegrationProject.DEVICE) {
			try {
				Entry<Byte, Byte> connection = null;

				Object temp = routerGetRoute.invoke(new Byte(d.getSource()));
				if (temp instanceof Entry)
					connection = (Entry<Byte, Byte>) temp;
				else
					throw new NullPointerException();

				byte ack = offer(d);

				send(new DataPacket(IntegrationProject.DEVICE, d.getSource(),
						connection.getValue(), ack, new byte[0], true, false,
						false, false));

			} catch (NullPointerException | CallbackException e1) {
				System.out
						.println("Error finding route for an ack! Possibly no route to that host. Which is strange, because somehow it did arrive.");
			} catch (BigPacketSentException | DatagramDataSizeException e) {
				// Can't really happen, but oh well...
				e.printStackTrace();
			}
		} else {
			d.decreaseHops();
			if (d.getHops() > 0) {
				try {
					send(d);
				} catch (BigPacketSentException e) {
					e.printStackTrace();
				}
			}
		}

	}
	
	public void resend(Byte destination, Byte sequencenumber, Boolean broadcast){
		
	}

	private BigPacket processPackets(LinkedList<DataPacket> packets) {
		DataPacket first = packets.peek();

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

		return new BigPacket(first.getSource(), first.getDestination(),
				first.getHops(), first.getSequenceNumber(), result,
				first.isAck(), first.isRouting(), first.isKeepAlive(), false);
	}

	private LinkedList<DataPacket> processData(Byte destination, Byte hops,
			byte[] data, boolean ack, boolean routing, boolean keepalive) {
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
						hops, sequencer.getTo(destination), chunk, ack,
						routing, keepalive, moar);
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

	private byte offer(DataPacket d) throws CallbackException{
		byte ack = sequencer.put(d);

		LinkedList<DataPacket> readyPackets = sequencer.getPackets(
				d.getSource(), false);
		LinkedList<DataPacket> buffer = new LinkedList<DataPacket>();

		while (!readyPackets.isEmpty()) {
			if (!readyPackets.peek().hasMore()) {
				packetReceived.invoke(readyPackets.poll());
			} else {
				while (!readyPackets.isEmpty()
						&& readyPackets.peek().hasMore()) {
					buffer.add(readyPackets.poll());
				}
				buffer.add(readyPackets.poll());

				packetReceived.invoke(processPackets(buffer));
			}
		}
		
		return ack;
	}

	private Byte dummyRoute(Byte b){
		return (byte) 0x0;
	}
}
