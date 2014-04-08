/**
 * 
 */
package routing;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import networking.DataPacket;
import main.Callback;
import monitoring.NetworkError;

/**
 * @author      Joey Haas <j.haas@student.utwente.nl>
 *
 */
public class LinkStateRouting implements RoutingInterface {
	
	TreeMap network = new TreeMap<Byte,ArrayList<Byte>>();
	
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

}
