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
	
	public DataPacket(byte[] raw) throws DatagramDataSizeException{
		if (raw.length < HEADER_LENGTH) throw new DatagramDataSizeException(raw.length);
		
		//TODO: Moar checks plzz
		
		packet = raw;
	}

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
	
	public void decreaseHops(){
		byte hops = getHops();
		
		if(!(hops == 0))
			packet[2] = (byte) ((hops - 1) | (packet[2] & 0xF0));
			
	}
	
	/*
	 * Getters
	 */
	
	public byte[] getRaw(){
		return packet;
	}
	
	public byte getSource(){
		return packet[0];
	}
	
	public byte getDestination(){
		return packet[1];
	}
	
	public byte[] getData(){
		byte[] buffer = new byte[packet.length - HEADER_LENGTH];
		System.arraycopy(packet, HEADER_LENGTH, buffer, 0, buffer.length);
		
		return buffer;
	}
	
	public byte getHops(){
		return (byte) (packet[2] & 0x0F);
	}
	
	public byte getSequenceNumber(){
		return packet[3];
	}
	
	public boolean isAck(){
		return ((packet[2] & ACK) == ACK);
	}
	
	public boolean isRouting(){
		return ((packet[2] & ROUTING) == ROUTING);
	}
	
	public boolean isKeepAlive(){
		return ((packet[2] & KEEP_ALIVE) == KEEP_ALIVE);
	}
	
	public boolean hasMore(){
		return ((packet[2] & MOAR) == MOAR);
	}
}
