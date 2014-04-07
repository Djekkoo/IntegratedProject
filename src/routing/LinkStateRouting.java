/**
 * 
 */
package routing;

import java.util.AbstractMap.SimpleEntry;

import networking.Networker;
import main.Callback;
import monitoring.NetworkError;
import monitoring.NetworkMonitor;

/**
 * @author joeyjo0
 *
 */
public class LinkStateRouting implements RoutingInterface {
	
	NetworkMonitor m = new NetworkMonitor();
	
	public LinkStateRouting(Callback poll, Callback send) {
		m.run();
	}
	
	@Override
	public void initialize() {
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
