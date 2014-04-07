/**
 * 
 */

/**
 * @author sander
 *
 */
public class DatagramBufferSizeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2054605277770717672L;

	public DatagramBufferSizeException(int size, int goal){
		super("Invalid data buffer size. The buffer size was " + size + " and should be " + goal + ".");
	}
}
