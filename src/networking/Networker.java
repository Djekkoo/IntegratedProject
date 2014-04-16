package networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;

import routing.LinkStateRouting;
import routing.RouteNotFoundException;
import routing.RoutingInterface;
import main.Callback;
import main.CallbackException;
import main.Communication;
import main.CryptoException;
import main.EncryptionHandler;
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

	RoutingInterface router;
	Communication communication;
	// EncryptionHandler eh;

	ReentrantLock lock;

	HashMap<Byte, HashMap<Byte, SmallPacket>> sends = new HashMap<Byte, HashMap<Byte, SmallPacket>>();

	/**
	 * 
	 * @param packetReceived
	 *            callback to function that handles incoming packets
	 * @throws IOException
	 */

	public Networker(Communication packetReceived) throws IOException {
		lock = new ReentrantLock();

		// eh = new EncryptionHandler("adhoc_key");

		sequencer = new Sequencer(new Callback(this, "resend"));
		multicastAddress = InetAddress.getByName(IntegrationProject.BROADCAST);

		dSock = new DatagramSocket(UNIPORT);
		mSock = new MulticastSocket(MULTIPORT);
		mSock.setLoopbackMode(true);
		mSock.joinGroup(multicastAddress);

		this.communication = packetReceived;

		(new UniMonitor(this, dSock)).start();

		(new MultiMonitor(this, mSock)).start();
	}

	/**
	 * Forms new DataPackets and broadcasts them
	 * 
	 * @param data
	 *            Data to be sent
	 * @param hops
	 *            Number of hops it can traverse
	 * @param nonSequence
	 *            Used if it should skip the sequencer
	 * @param routing
	 *            Is used by routing protocol
	 * @param keepalive
	 *            Is used as a keep alive signal
	 * @throws IOException
	 *             When the socket can be reached
	 * @throws DatagramDataSizeException
	 */
	public void broadcast(byte[] data, Byte hops, Boolean nonSequence,
			Boolean routing, Boolean keepalive) throws IOException,
			DatagramDataSizeException {

		SmallPacket dp = new SmallPacket(IntegrationProject.DEVICE,
				(byte) 0x0F, (byte) 0x0, (byte) 0x0, data, false, routing,
				keepalive, false);
		mSock.send(new DatagramPacket(dp.getRaw(), dp.getRaw().length,
				multicastAddress, MULTIPORT));

	}

	/**
	 * Broadcasts a packet that is already formed
	 * 
	 * @param dp
	 *            Preformed DataPacket
	 * @throws IOException
	 *             When the socket can be reached
	 * @throws BigPacketSentException
	 */

	public void broadcast(DataPacket dp) throws IOException,
			BigPacketSentException {
		if (dp instanceof BigPacket)
			throw new BigPacketSentException();

		mSock.send(new DatagramPacket(dp.getRaw(), dp.getRaw().length,
				multicastAddress, MULTIPORT));

	}

	/**
	 * Sends data to a specific host
	 * 
	 * @param destination
	 *            The end node it should be passed on to
	 * @param data
	 *            The data to be sent
	 * @param routing
	 *            Whether or not it is meant for routing. This will skip sequencer
	 * @throws IOException
	 *             When the socket can be reached
	 */
	@SuppressWarnings("unchecked")
	public void send(Byte destination, byte[] data, Boolean routing)
			throws IOException {

		HashMap<Byte, SmallPacket> entry;

		Entry<Byte, Byte> connection = null;

		try {
			Object temp = router.getRoute(new Byte(destination));
			if (temp instanceof Entry)
				connection = (Entry<Byte, Byte>) temp;
		} catch (RouteNotFoundException e) {
			e.printStackTrace();
			return;
		}

		LinkedList<SmallPacket> packets = processData(destination,
				connection.getValue(), data, false, routing, false);

		if (sends.containsKey(destination)) {
			entry = sends.get(destination);
		} else {
			entry = new HashMap<Byte, SmallPacket>();
		}

		for (SmallPacket p : packets) {
			entry.put(p.getSequenceNumber(), p);

			dSock.send(new DatagramPacket(p.getRaw(), p.getRaw().length,
					getFullAddress(connection.getKey()), UNIPORT));
		}

		sends.put(destination, entry);

	}

	public void send(Byte destination, byte[] data) throws IOException {
		send(destination, data, false);
	}

	/**
	 * Sends a preformed DataPacket
	 * 
	 * @param d
	 *            The DataPacket it should send
	 * @throws IOException
	 *             When the socket can be reached
	 */
	@SuppressWarnings("unchecked")
	public void send(DataPacket d) throws IOException, BigPacketSentException {

		if (d instanceof BigPacket)
			throw new BigPacketSentException();

		HashMap<Byte, SmallPacket> entry;
		if (sends.containsKey(d.getDestination())) {
			entry = sends.get(d.getDestination());
		} else {
			entry = new HashMap<Byte, SmallPacket>();
		}
		entry.put(d.getSequenceNumber(), (SmallPacket) d);
		sends.put(d.getDestination(), entry);

		Entry<Byte, Byte> connection;

		try {
			Object temp = router.getRoute(d.getDestination());
			if (temp instanceof Entry)
				connection = (Entry<Byte, Byte>) temp;
			else
				throw new NullPointerException();
		} catch (NullPointerException | RouteNotFoundException e1) {
			System.out
					.println("Error finding route. Possibly no route to that host.");
			return;
		}

		dSock.send(new DatagramPacket(d.getRaw(), d.getRaw().length,
				getFullAddress(connection.getKey()), UNIPORT));

	}

	public void send(SmallPacket dp, InetAddress port)
			throws BigPacketSentException, IOException {

		if (dp instanceof BigPacket)
			throw new BigPacketSentException();

		HashMap<Byte, SmallPacket> entry;
		if (sends.containsKey(dp.getDestination())) {
			entry = sends.get(dp.getDestination());
		} else {
			entry = new HashMap<Byte, SmallPacket>();
		}
		entry.put(dp.getSequenceNumber(), dp);
		sends.put(dp.getDestination(), entry);

		dSock.send(new DatagramPacket(dp.getRaw(), dp.getRaw().length, port,
				UNIPORT));
	}

	/**
	 * When one of the monitors receives a DataPacket it should pass it on to
	 * here
	 * 
	 * @param d
	 *            The received DataPacket
	 * @throws IOException
	 *             When the socket can be reached
	 */

	@SuppressWarnings("unchecked")
	public void receive(SmallPacket d, Inet4Address port) throws IOException {
		if (d.getDestination() == (byte) 0x0F) {// Multicast

			communication.newPacket(d);

			if (d.getHops() > 0) {
				d.decreaseHops();
				try {
					broadcast(d);
				} catch (BigPacketSentException e) {
					System.out
							.println("Received a BigPacket, apparently... Which cannot happen. Really. It should have crashed earlier.");
				}
			}
		} else if (d.getDestination() == IntegrationProject.DEVICE) {
			if (d.getSequenceNumber() == 0) {
				if (!d.isAck() && !d.isKeepAlive() && !d.isRouting()
						&& !d.hasMore()) {

					System.out.println("Got a handshake!");

					if (d.getData().length == 1) {
						sequencer
								.setSequenceFrom(d.getSource(), d.getData()[0]);
					} else {
						System.out.println(EncryptionHandler
								.parsePubKeyPacket(d.getData()));
						// eh.addPubKey(d.getSource(),
						// EncryptionHandler.parsePubKeyPacket(d.getData()));
					}
					return; // Job done, no bubbling up
				} else if (d.isRouting() && !d.isAck() && !d.isKeepAlive()
						&& !d.hasMore()) {
					router.packetReceived(d);
					
					return;
				}
			}

			try {
				if (d.getSource() == port.getAddress()[3]) {
					byte ack = offer((SmallPacket) d);
					if (ack != (byte) 0)
						send(new SmallPacket(IntegrationProject.DEVICE,
								d.getSource(), (byte) 0x0, ack, new byte[0],
								true, false, false, false), port);
				} else {

					Entry<Byte, Byte> connection = null;

					System.out.println("Source to find route to: "
							+ d.getSource());
					Object temp = router.getRoute(new Byte(d
							.getSource()));

					if (temp instanceof Entry)
						connection = (Entry<Byte, Byte>) temp;
					else
						throw new NullPointerException();

					byte ack = offer((SmallPacket) d);

					if (ack != (byte) 0)
						send(new SmallPacket(IntegrationProject.DEVICE,
								d.getSource(), connection.getValue(), ack,
								new byte[0], true, false, false, false));
				}

			} catch (CallbackException e1) {
				System.out
						.println("Error finding route for an ack! Possibly no route to that host. Which is strange, because somehow it did arrive.");
				e1.getException().printStackTrace();
			} catch (NullPointerException | BigPacketSentException
					| DatagramDataSizeException e) {
				// Can't really happen, but oh well...
				e.printStackTrace();
			} catch (RouteNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else { // Must pass it on
			d.decreaseHops();
			if (d.getHops() > 0) {
				try {
					send(d);
				} catch (BigPacketSentException e) {
					// Can't really happen, but oh well...
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * 
	 * @param destination
	 *            Destination host
	 * @param sequencenumber
	 *            Number to resend
	 * @param broadcast
	 *            Whether or not it was a broadcast
	 */

	public void resend(Byte destination, Byte sequencenumber) {
		try {
			send(sends.get(destination).get(sequencenumber));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (BigPacketSentException e) {
			e.printStackTrace();
		} finally {
			System.out.println("Attempted resend of " + sequencenumber + " to "
					+ destination);
		}
	}

	/**
	 * Should be called when a new node is discovered. It generates a new
	 * sequencenumber and sends it 3 times to the newly discovered node.
	 * 
	 * @param destination
	 */
	@SuppressWarnings("unchecked")
	public void handshake(Byte destination) {
		System.out.println("Handshaking initialized");
		lock.lock();
		byte sequence = (byte) (new Random()).nextInt();
		while (sequence == 0)
			sequence = (byte) (new Random()).nextInt();

		sequencer.setSequenceTo(destination, sequence);

		System.out.println("Sequencer aware of new sequencenr: " + sequence);

		Entry<Byte, Byte> connection = null;
		try {
			Object temp = router.getRoute(new Byte(destination));
			if (temp instanceof Entry)
				connection = (Entry<Byte, Byte>) temp;
		} catch (RouteNotFoundException e1) {
			System.out
					.println("Error finding route. Possibly no route to that host.");
			lock.unlock();
			return;
		}

		SmallPacket dp;
		try {
			dp = new SmallPacket(IntegrationProject.DEVICE, destination,
					connection.getValue(), (byte) 0x0, new byte[] { sequence },
					false, false, false, false);
			send(dp); // We cannot assume
			send(dp); // the first packet
			send(dp); // actually arrives

			// dp = new SmallPacket(IntegrationProject.DEVICE, destination,
			// connection.getValue(), (byte) 0x0, eh.getPubKeyPacket(),
			// false, false, false, false);
			// send(dp); // We cannot assume
			// send(dp); // the first packet
			// send(dp); // actually arrives
		} catch (IOException | BigPacketSentException
				| DatagramDataSizeException e) {
			System.out.println("BAM JONGÛH!");
			e.printStackTrace();
		}
		lock.unlock();
		System.out.println("Handshake finished");
	}

	/**
	 * Sets the callback for retrieving a route to a node
	 * 
	 * @param router
	 *            Callback to the function of Routing
	 */
	public void setRouter(RoutingInterface router) {
		System.out.println("Set router!");
		this.router = router;

	}

	private BigPacket processPackets(LinkedList<DataPacket> packets) {
		DataPacket first = packets.peek();

		int maxChunkSize = 1024 - SmallPacket.HEADER_LENGTH;
		int length = 0;
		if (packets.size() > 0)
			length = (packets.size() - 1) * maxChunkSize
					+ packets.getLast().getData().length;

		byte[] result = new byte[length];
		byte[] buffer = new byte[0];

		int i = 0;
		while (!packets.isEmpty()) {
			buffer = packets.pop().getData();
			System.arraycopy(buffer, 0, result, i * maxChunkSize, buffer.length);
			i++;
		}
		//
		// try {
		// result = eh.parsePacket(result, first.getSource());
		// } catch (CryptoException e) {
		// System.out.println(e.getMessage());
		// }

		return new BigPacket(first.getSource(), first.getDestination(),
				first.getHops(), first.getSequenceNumber(), result,
				first.isAck(), first.isRouting(), first.isKeepAlive(), false);
	}

	private LinkedList<SmallPacket> processData(Byte destination, Byte hops,
			byte[] data, boolean ack, boolean routing, boolean keepalive) {

		// data = eh.getPacket(data, destination);

		LinkedList<SmallPacket> result = new LinkedList<SmallPacket>();
		lock.lock();
		SmallPacket dp;

		boolean moar = false;

		int maxChunkSize = 1024 - SmallPacket.HEADER_LENGTH;
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
				Byte sequencenr = 0;
				if (!routing)
					sequencenr = sequencer.getTo(destination);

				if (!(sequencenr == null)) {
					dp = new SmallPacket(IntegrationProject.DEVICE,
							destination, hops, sequencenr, chunk, ack, routing,
							keepalive, moar);
					result.add(dp);
				} else {
					System.out
							.println("Dropped packet because getTo return NULL GODDAMMIT!");
				}
			} catch (DatagramDataSizeException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				System.out.println("Fuck this");
				e.printStackTrace();
			}
		}

		lock.unlock();
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

	private byte offer(SmallPacket d) throws CallbackException {
		System.out.println("Offering");
		Byte ack = sequencer.put(d);

		if (ack == null)
			return 0;

		System.out.println("Getting packets");
		LinkedList<DataPacket> readyPackets = sequencer.getPackets(d
				.getSource());

		System.out.println("YAY! " + readyPackets.size()
				+ " packets to process! WOOOO!");

		LinkedList<DataPacket> buffer = new LinkedList<DataPacket>();

		System.out.println("Looping packets");
		while (!readyPackets.isEmpty()) {
			if (!readyPackets.peek().hasMore()) {
				buffer.add(readyPackets.poll());
				communication.newPacket(processPackets(buffer));
			} else {
				while (!readyPackets.isEmpty() && readyPackets.peek().hasMore()) {
					buffer.add(readyPackets.poll());
				}
				buffer.add(readyPackets.poll());

				communication.newPacket(processPackets(buffer));
			}
			buffer = new LinkedList<DataPacket>();
		}

		return ack;
	}

	@SuppressWarnings("unused")
	private Byte dummyRoute(Byte b) {
		return (byte) 0x0;
	}
}
