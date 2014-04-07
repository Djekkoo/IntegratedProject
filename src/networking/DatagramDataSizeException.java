package networking;
/**
 * 
 */

/**
 * @author sander
 * 
 */
public class DatagramDataSizeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2054605277770717672L;

	public DatagramDataSizeException(int size) {
		super("Invalid data buffer size. The buffer size was " + size
				+ " and should be 1024.");
	}
}
