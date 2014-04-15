package routing;

import java.util.AbstractMap.SimpleEntry;

import networking.DataPacket;
import monitoring.NetworkMessage;

/**
 * A basic interface for all routing.
 * 
 * @author      Joey Haas <j.haas@student.utwente.nl>
 * @version     0.1
 * @since       2014-04-07
 */
public interface RoutingInterface {
	
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
	public void networkMessage(Byte node, NetworkMessage type);
	
	/**
	 * Checks whether a node is still reachable.
	 * 
	 * @param	node The node to check the connection to
	 * @return 	True if the node still has a route.
	 * @return	False if the node has no known route.
	 */
	public Boolean isReachable(Byte node);
	
	/**
	 * Returns the longest route in the entire network.
	 * 
	 * @return 	The amount of hops the longest route takes.
	 */
	public Byte getLongestRoute();
	
	/**
	 * Returns a list of all devices on the network.
	 * 
	 * @return 	The list of devices on the network.
	 */
	public Byte[] getDevices();
}
