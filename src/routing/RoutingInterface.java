package routing;

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
	 * Gets the fastest route to a given destination.
	 * 
	 * @param 	destination Destination to find route to.
	 * @return 	The last byte of the IP for the next hop.
	 */
	public byte getRoute(byte destination) throws RouteNotFoundException;
}
