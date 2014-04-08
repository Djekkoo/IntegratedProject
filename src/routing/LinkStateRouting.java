/**
 * 
 */
package routing;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import networking.DataPacket;
import main.Callback;
import monitoring.NetworkMessage;

/**
 * Implements a Link-State routing protocol to explore the network,
 * then uses pathfinding to find the shortest paths.
 * 
 * @author      Joey Haas <j.haas@student.utwente.nl>
 * @version     0.1
 * @since       2014-04-07
 */
public class LinkStateRouting implements RoutingInterface {
	
	private TreeMap<Byte,TreeSet<Byte>> nw = new TreeMap<Byte,TreeSet<Byte>>();
	private long lastReceivedPacket = 0;
	private long lastSentPacket = 0;
	
	public LinkStateRouting(Callback send) {
		showNetwork();
		Byte[] b = buildPacket();
		nw.clear();
		int j = 0;
		byte[] pack = new byte[b.length];
		for(Byte by: b){
			pack[j++] = by.byteValue();
		}
		parsePacket(pack);
		showNetwork();
		System.out.println("Done.");
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() {
		// TODO Initialize 
		// Explore network and add neighbours.
	}

	public static void main(String[] args) {
		RoutingInterface r = new LinkStateRouting(null);
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void packetReceived(DataPacket p) {
		// TODO Handle packet receives
		boolean updated = parsePacket(p.getData());
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public SimpleEntry<Byte, Byte> getRoute(Byte destination)
			throws RouteNotFoundException {
		// TODO Respond whenever a route is requested.
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void networkMessage(NetworkMessage type, Byte node) {
		// TODO Handle messages
		
	}

	/**
	 * Builds the routing packet to be sent to the neighbours.
	 * Done by serializing the network data and adding metadata;
	 * 
	 * @param	p	The bytes of the packet to be parsed.
	 * @return 	True if the new packet caused the network tree to update,
	 * @return	False if no changes were made.
	 * @since	2014-04-08
	 */
	private boolean parsePacket(byte[] p) {
		//Retrieve the timestamp from the packet
		long timestamp = 0;
	    ByteBuffer buffer = ByteBuffer.wrap(p,0,8);
	    timestamp = buffer.getLong();
		boolean updated = false;
		
		//Is the packet newer than the last received packet?
		if(timestamp > lastReceivedPacket) {
			lastReceivedPacket = timestamp;
			int numHosts = p[8];
			int offset = 9;
			for(int i = 0; i < numHosts; i++) {
				byte host = p[0+offset];
				int numNeighbours = p[1+offset];
				byte[] neighbours = new byte[numNeighbours];
				System.arraycopy(p, 2+offset, neighbours, 0, numNeighbours);
				offset += numNeighbours+2;
				for(byte b : neighbours) {
					//Do we have a host that we have no record of?
					if(nw.containsKey(host)) {
						((TreeSet<Byte>)nw.get(host)).add(b);
					} else {
						//I nominate the following two lines for
						//	the "least readable code ever"-awards
						nw.put(host, new TreeSet<Byte>());
						((TreeSet<Byte>)nw.get(host)).add(b);
						updated = true;
					}
				}
			}
		}
		return updated;
	}
	
	/**
	 * Builds the routing packet to be sent to the neighbours.
	 * Done by serializing the network data and adding metadata;
	 * 
	 * @return 	The Byte array containing the serialized network data and metadata.
	 * @since	2014-04-08
	 */
	private Byte[] buildPacket() {
		ArrayList<Byte> p = new ArrayList<Byte>();
		
		// Add timestamp to the first 8 bits.
		long timestamp = new Date().getTime();
		lastSentPacket = timestamp;
		byte[] timestampBytes = ByteBuffer.allocate(8).putLong(timestamp).array();
		for(byte b : timestampBytes) {
			p.add((Byte)b);
		}
		
		// Add the host count.
		p.add(Byte.valueOf((byte)(nw.size()&0xFF)));
		for (Entry<Byte,TreeSet<Byte>> e : nw.entrySet()) {
			Byte host = e.getKey();
			TreeSet<Byte> neighbours = e.getValue();
			p.add(host);
			p.add(Byte.valueOf((byte)(neighbours.size()&0xFF)));
			for(Byte b : neighbours) {
				p.add(b);
			}
		}
		
		//A really hacky way to cast byte[] into Byte[]
		Byte[] bytes = new Byte[p.size()];
		int i = 0;
		for(Object obj : p.toArray()) {
			Byte b = (Byte)obj;
			bytes[i++] = b;
		}
		
		return bytes;
	}
	
	private void showNetwork() {
		for(Entry<Byte,TreeSet<Byte>> e : nw.entrySet()) {
			System.out.println(e.getKey() + " to: ");
			for(Byte b : e.getValue()) {
				System.out.println("\t" + b);
			}
		}
	}
}
