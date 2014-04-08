/**
 * 
 */
package routing;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import networking.DataPacket;
import main.Callback;
import monitoring.NetworkError;

/**
 * @author      Joey Haas <j.haas@student.utwente.nl>
 *
 */
public class LinkStateRouting implements RoutingInterface {
	
	private TreeMap nw = new TreeMap<Byte,Set<Byte>>();
	private int lastPacket = 0;
	
	public LinkStateRouting(Callback send) {
		Set<Byte> s = new TreeSet<Byte>();
		s.add(Byte.valueOf((byte)0x2));
		s.add(Byte.valueOf((byte)0x4));
		this.nw.put(Byte.valueOf((byte)0x4), s);
		
		Byte[] b = buildPacket();
		nw.clear();
		parsePacket(b);
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

	private boolean parsePacket(byte[] p) {
		long timestamp = 0;
		for(int i = 0; i < 8; i++) {
			timestamp |= (p[i]<<48-(i*8));
		}
		boolean updated = false;
		int numHosts = p[8];
		int offset = 9;
		for(int i = 0; i < numHosts; i++) {
			int host = p[0+offset];
			int numNeighbours = p[1+offset];
			byte[] neighbours = new byte[numNeighbours];
			System.arraycopy(p, 2+offset, neighbours, 0, numNeighbours);
			offset += numNeighbours+2;
			for(byte b : neighbours) {
				((Set<Byte>)nw.get(host)).add(b);
			}
			if(nw.containsKey(host)) {
				updated = true;
			}
		}
		return updated;
	}
	
	private Byte[] buildPacket() {
		ArrayList<Byte> p = new ArrayList<Byte>();
		long timestamp = new Date().getTime();
		for(int i = 0; i<8; i++) {
			p.add(Byte.valueOf((byte)(timestamp & (0xFF<<48-(i*8)))));
		}
		p.add(Byte.valueOf((byte)(nw.size()&0xFF)));
		for (Object obj : nw.entrySet()) {
			Entry<Byte,ArrayList<Byte>> e = (Entry<Byte,ArrayList<Byte>>)obj;
			Byte host = e.getKey();
			ArrayList<Byte> neighbours = e.getValue();
			p.add(host);
			p.add(Byte.valueOf((byte)(neighbours.size()&0xFF)));
			for(Byte b : neighbours) {
				p.add(b);
			}
		}
		return (Byte[])p.toArray();
	}
}
