/**
 * 
 */
package routing;

import java.nio.ByteBuffer;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import networking.DataPacket;
import main.Callback;
import main.CallbackException;
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
	private Callback send;
	private byte DEVICE;
	
	public LinkStateRouting(Callback send) {
		this.send = send;
		this.DEVICE = main.IntegrationProject.DEVICE;
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
	//SimpleEntry: NextHop, Distance
	public SimpleEntry<Byte, Byte> getRoute(Byte destination)
			throws RouteNotFoundException {
		// TODO Respond whenever a route is requested.
		// TODO Pathfinding
		
		if(!nw.containsKey(destination))
			throw new RouteNotFoundException("Destination unknown.");
		
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void networkMessage(NetworkMessage type, Byte node) {
		// TODO Handle messages
		try{
			switch(type) {
			case NEWKEEPALIVE:
				nw.get(DEVICE).add(node);
				send.invoke(node,buildPacket());
				break;
			case DROPPED:
				nw.get(DEVICE).remove(node);
				break;
			case NOKEEPALIVE:
				break;
			default:
				break;
			}
		} catch(CallbackException e) {
			e.printStackTrace();
		}
	}
	
	private void sendToNeighbours(byte[] data) {
		TreeSet<Byte> neighbours = nw.get(DEVICE);
		Byte[] p = buildPacket();
		for(Byte nb : neighbours) {
			send.invoke(nb,toByteArray(p));
		}
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
			//Get how many hosts there are in the data
			int numHosts = p[8];
			int offset = 9;
			for(int i = 0; i < numHosts; i++) {
				//Get the host of the entry
				byte host = p[0+offset];
				//Get how many neighbours are in the entry
				int numNeighbours = p[1+offset];
				byte[] neighbours = new byte[numNeighbours];
				System.arraycopy(p, 2+offset, neighbours, 0, numNeighbours);
				offset += numNeighbours+2;
				
				TreeSet<Byte> oldNeighbours = new TreeSet<Byte>();
				for(Object nb : nw.get(host).toArray()) {
					oldNeighbours.add((Byte)nb);
				}
				
				for(byte nb : neighbours) {
					//Do we have a host that we have no record of?
					if(nw.containsKey(host)) {
						nw.get(host).add(nb);
						oldNeighbours.remove(nb);
					} else {
						nw.put(host, new TreeSet<Byte>());
						nw.get(host).add(nb);
						oldNeighbours.remove(nb);
						updated = true;
					}
				}
				for(Byte nb : oldNeighbours) {
					nw.remove(nb);
					updated = true;
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
		
		return toByteObjectArray(p);
	}
	
	private void showNetwork() {
		for(Entry<Byte,TreeSet<Byte>> e : nw.entrySet()) {
			System.out.println(e.getKey() + " to: ");
			for(Byte b : e.getValue()) {
				System.out.println("\t" + b);
			}
		}
	}
	
	private Byte[] toByteObjectArray(Object[] bytes) {
		//ArrayList<Byte> into Byte[]
		Byte[] byteObjects = new Byte[bytes.length];
		int i = 0;
		for(Object b : bytes) {
			Byte bObj = (Byte)b;
			byteObjects[i++] = bObj;
		}
		return byteObjects;
	}
	
	private Byte[] toByteObjectArray(byte[] bytes) {
		//A really hacky way to cast byte[] to Byte[]
		Byte[] byteObjects = new Byte[bytes.length];
		int i = 0;
		for(byte b : bytes) {
			Byte bObj = (Byte)b;
			byteObjects[i++] = bObj;
		}
		return byteObjects;
	}
	
	private byte[] toByteArray(Byte[] byteObjects) {
		//A really hacky way to cast Byte[] to byte[]
		byte[] bytes = new byte[byteObjects.length];
		int i = 0;
		for(Byte bObj : byteObjects) {
			Byte b = bObj.byteValue();
			bytes[i++] = b;
		}
		return bytes;
	}
}
