package networking;

/** 
 * DatagramDataSizeException is thrown when a DataPacket is instantiated 
 * with a byte array exceeding the maximum size.
 * 
 * @author      Sander Koning <s.koning@student.utwente.nl>
 * @version     0.1
 * @since       2014-04-07
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
