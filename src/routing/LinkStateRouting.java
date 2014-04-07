/**
 * 
 */
package routing;

import java.util.AbstractMap.SimpleEntry;

import networking.Networker;
import monitoring.NetworkError;

/**
 * @author joeyjo0
 *
 */
public class LinkStateRouting implements RoutingInterface {

	@Override
	public void initialize(Networker n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SimpleEntry<Byte, Byte> getRoute(byte destination)
			throws RouteNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void networkError(NetworkError type, byte node) {
		// TODO Auto-generated method stub

	}

}
