/**
 * 
 */
package routing;

import java.util.AbstractMap.SimpleEntry;

import main.Callback;
import monitoring.NetworkError;

/**
 * @author      Joey Haas <j.haas@student.utwente.nl>
 *
 */
public class LinkStateRouting implements RoutingInterface {
	
	public LinkStateRouting(Callback poll, Callback send) {
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() {
		// TODO Auto-generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SimpleEntry<Byte, Byte> getRoute(Byte destination)
			throws RouteNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void networkError(NetworkError type, Byte node) {
		// TODO Auto-generated method stub
		
	}

}
