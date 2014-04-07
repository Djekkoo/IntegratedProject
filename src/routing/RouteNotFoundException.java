package routing;

/**
 * Standard exception, thrown if the route to the destination node cannot be found.
 * 
 * @author      Joey Haas <j.haas@student.utwente.nl>
 * @version     0.1
 * @since       2014-04-07
 */
public class RouteNotFoundException extends Exception {
	
	private static final long serialVersionUID = -2482656411978162877L;

	public RouteNotFoundException() {
		super();
	}
	
	public RouteNotFoundException(String message) {
		super(message);
	}
	
	public RouteNotFoundException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
