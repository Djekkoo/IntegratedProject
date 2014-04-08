/**
 * 
 */
package routing;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import networking.DataPacket;
import main.Callback;
import monitoring.NetworkError;

/**
 * @author      Joey Haas <j.haas@student.utwente.nl>
 *
 */
public class LinkStateRouting implements RoutingInterface {
	
	TreeMap nw = new TreeMap<Byte,ArrayList<Byte>>();
	
	public LinkStateRouting(Callback poll, Callback send) {
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() {
		// TODO Initialize 
		// Explore network and add neighbours.
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
	public void networkError(NetworkError type, Byte node) {
		// TODO Handle errors
		
	}

	private TreeMap<Byte,ArrayList<Byte>> parsePacket(byte[] p) {
		int hosts = p[0];
		return null;
	}
	
	private Byte[] buildPacket() {
		ArrayList<Byte> p = new ArrayList<Byte>();
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
