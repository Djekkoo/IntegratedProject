package networking;

/** 
 * DatagramDataSizeException is thrown when a BigPacket is offered to be sent.
 * 
 * @author      Sander Koning <s.koning@student.utwente.nl>
 * @version     0.1
 * @since       2014-04-07
 */

public class BigPacketSentException extends Exception {

	private static final long serialVersionUID = -9175040186729872396L;

	public BigPacketSentException() {
		super("Cannot send a BigPacket on link. Please properly process into generic DataPackets.");
	}
}
