package networking;

/**
 * This class will form objects that form packets to be sent over UDP.
 * 
 * There are two constructors:
 * 	One constructor takes all header fields and the data.
 *  The other constructor takes the raw packet.
 *
 * @author      Sander Koning <s.koning@student.utwente.nl>
 * @version     1.0-b
 * @since       2014-04-08
 */

public class DataPacket{
	
	public static final int HEADER_LENGTH = 4;
	
	protected static final byte ACK = (byte) 0x10;
	protected static final byte ROUTING = (byte) 0x20;
	protected static final byte KEEP_ALIVE = (byte) 0x40;
	protected static final byte MOAR = (byte) 0x80;
	
	protected byte[] packet = new byte[0];
	
	protected DataPacket(){
		// Can now be extended by BigPacket
	}
	
	/**
	 * Construct DataPacket from raw data received over a link
	 * 
	 * @param raw Raw packet data (including headers)
	 * @throws DatagramDataSizeException
	 */
	
	public DataPacket(byte[] raw) throws DatagramDataSizeException{
		if (raw.length < HEADER_LENGTH) throw new DatagramDataSizeException(raw.length);
		
		//TODO: Moar checks plzz
		
		packet = raw;
	}
	
	/**
	 * Build packet from scratch.
	 * 
	 * @param source The source
	 * @param destination The destination
	 * @param hops Number of hops
	 * @param sequencenr Sequencenumber to identify packets 
	 * @param data Array of bytes with raw data, cannot exceed size 1024-HEADER_SIZE
	 * @param ack The packet is an ack
	 * @param routing The packet is meant for routing
	 * @param keepalive The packet is a keep alive signal
	 * @param more Should be set if the packet with sequencenumber + 1 is part of this packet
	 */

	public DataPacket(byte source, byte destination, byte hops, byte sequencenr, byte[] data, Boolean ack, Boolean routing, Boolean keepalive, Boolean more) throws DatagramDataSizeException{

		if(data.length > (1024 - HEADER_LENGTH)) throw new DatagramDataSizeException(data.length + HEADER_LENGTH);
		
		packet = new byte[data.length + HEADER_LENGTH];
		
		System.arraycopy(data, 0, packet, HEADER_LENGTH, data.length);
		
		packet[0] = source;
		packet[1] = destination;
		
		if(hops > 0x0F) hops = 0x0F;
		
		packet[2] = (byte) (hops & 0x0F);

		if(ack) 		packet[2] = (byte) (packet[2] | ACK);
		if(routing) 	packet[2] = (byte) (packet[2] | ROUTING);
		if(keepalive) 	packet[2] = (byte) (packet[2] | KEEP_ALIVE);
		if(more) 		packet[2] = (byte) (packet[2] | MOAR);
		
		packet[3] = sequencenr;
		
	}
	
	/*
	 * Setters
	 */
	
	/**
	 * Decreases hops with a minimum of 0
	 */
	
	public void decreaseHops(){
		byte hops = getHops();
		
		if(!(hops == 0))
			packet[2] = (byte) ((hops - 1) | (packet[2] & 0xF0));
			
	}
	
	/*
	 * Getters
	 */
	
	/**
	 * 
	 * @return Array of bytes with data and headers
	 */
	public byte[] getRaw(){
		return packet;
	}
	
	/**
	 * 
	 * @return Source
	 */
	public byte getSource(){
		return packet[0];
	}
	
	/**
	 * 
	 * @return Destination
	 */
	public byte getDestination(){
		return packet[1];
	}
	
	/**
	 * 
	 * @return Array of bytes with data without no headers
	 */
	public byte[] getData(){
		byte[] buffer = new byte[packet.length - HEADER_LENGTH];
		System.arraycopy(packet, HEADER_LENGTH, buffer, 0, buffer.length);
		
		return buffer;
	}
	
	/**
	 * 
	 * @return Hops
	 */
	public byte getHops(){
		return (byte) (packet[2] & 0x0F);
	}
	
	/**
	 * 
	 * @return Sequencenumber
	 */
	public byte getSequenceNumber(){
		return packet[3];
	}
	
	/**
	 * 
	 * @return is an ACK
	 */
	public boolean isAck(){
		return ((packet[2] & ACK) == ACK);
	}
	
	/**
	 * 
	 * @return is used for routing protocol
	 */
	public boolean isRouting(){
		return ((packet[2] & ROUTING) == ROUTING);
	}
	
	/**
	 * 
	 * @return is used as a keep alive signal
	 */
	public boolean isKeepAlive(){
		return ((packet[2] & KEEP_ALIVE) == KEEP_ALIVE);
	}
	
	/**
	 * 
	 * @return is fragmented and not the last packet
	 */
	public boolean hasMore(){
		return ((packet[2] & MOAR) == MOAR);
	}
}
