package routing;

import java.util.AbstractMap.SimpleEntry;

import networking.DataPacket;
import networking.Networker;
import monitoring.NetworkError;

/**
 * @author      Joey Haas <j.haas@student.utwente.nl>
 * @version     0.1
 * @since       2014-04-07
 */
public interface RoutingInterface {
	
	/**
	 * Sets the routing system up.
	 */
	public void initialize();
	
	/**
	 * Called if a packet is received.
	 * 
	 * @param 	p The data packet that is received
	 */
	public void packetReceived(DataPacket p);
	
	/**
	 * Gets the fastest route to a given destination.
	 * 
	 * @param 	destination Destination to find route to.
	 * @return 	The last byte of the IP for the next hop and the hop count.
	 */
	public SimpleEntry<Byte,Byte> getRoute(Byte destination) throws RouteNotFoundException;
	
	/**
	 * Called if a network error occurrs.
	 * 
	 * @param 	type Network Error type
	 * @param 	node The node that caused the error
	 */
	public void networkError(NetworkError type, Byte node);
}
