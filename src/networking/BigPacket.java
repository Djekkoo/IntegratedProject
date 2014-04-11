package networking;

/**
 * This class is a viritual DataPacket. It may exceed the 1024 size limit and thus cannot be sent.
 * 
 * The constructor takes all header fields and the data.
 *
 * @author      Sander Koning <s.koning@student.utwente.nl>
 * @version     1.0-b
 * @since       2014-04-08
 */

public class BigPacket extends DataPacket {
	
	/**
	 * 
	 * @param source The source
	 * @param destination The destination
	 * @param hops Number of hops
	 * @param sequencenr Sequencenumber to identify packets 
	 * @param data Array of bytes with raw data
	 * @param ack The packet is an ack
	 * @param routing The packet is meant for routing
	 * @param keepalive The packet is a keep alive signal
	 * @param more Should be set if the packet with sequencenumber + 1 is part of this packet
	 */

	public BigPacket(byte source, byte destination, byte hops, byte sequencenr,
			byte[] data, Boolean ack, Boolean routing, Boolean keepalive,
			Boolean more) {
		
		packet = new byte[data.length + HEADER_LENGTH];

		System.arraycopy(data, 0, packet, HEADER_LENGTH, data.length);

		packet[0] = source;
		packet[1] = destination;

		if (hops > 0x0F)
			hops = 0x0F;

		packet[2] = (byte) (hops & 0x0F);

		if (ack)
			packet[2] = (byte) (packet[2] | ACK);
		if (routing)
			packet[2] = (byte) (packet[2] | ROUTING);
		if (keepalive)
			packet[2] = (byte) (packet[2] | KEEP_ALIVE);
		if (more)
			packet[2] = (byte) (packet[2] | MOAR);

		packet[3] = sequencenr;
	}

}
