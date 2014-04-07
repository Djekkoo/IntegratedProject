package networking;

public class DataPacket{
	
	private static final byte ACK = (byte) 0x10;
	private static final byte ROUTING = (byte) 0x20;
	private static final byte KEEP_ALIVE = (byte) 0x40;
	
	private byte[] packet = new byte[1024];
	
	public DataPacket(byte[] raw) throws DatagramDataSizeException{
		if (raw.length < 8) throw new DatagramDataSizeException(raw.length);
		
		//TODO: Moar checks plzz
		
		packet = raw;
	}

	public DataPacket(byte source, byte destination, byte hops, byte sequencenr, byte[] data, boolean ack, boolean routing, boolean keepalive){
		packet[0] = source;
		packet[1] = destination;
		
		if(hops > 0x0F) hops = 0x0F;
		
		packet[3] = (byte) (hops & 0x0F);

		if(ack) 		packet[3] |= ACK;
		if(routing) 	packet[3] |= ROUTING;
		if(keepalive) 	packet[3] |= KEEP_ALIVE;
		
		packet[4] = sequencenr;
		
		if(data.length <= (1024 - 8))
			System.arraycopy(data, 0, packet, 8, data.length);
	}
	
	/*
	 * Setters
	 */
	
	public byte[] getRaw(){
		return packet;
	}
	
	public byte[] getData(){
		byte[] buffer = new byte[packet.length - 8];
		System.arraycopy(packet, 8, buffer, 0, buffer.length);
		
		return buffer;
	}
	
	public void decreaseHops(){
		byte hops = getHops();
		
		if(!(hops == 0))
			packet[3] = (byte) ((hops - 1) | (packet[3] & 0xF0));
			
	}
	
	/*
	 * Getters
	 */
	
	public byte getHops(){
		return (byte) (packet[3] & 0x0F);
	}
	
	public byte getSequenceNumber(){
		return packet[4];
	}
	
	public boolean isAck(){
		return ((packet[3] & ACK) == ACK);
	}
	
	public boolean isRouting(){
		return ((packet[3] & ROUTING) == ROUTING);
	}
	
	public boolean isKeepAlive(){
		return ((packet[3] & KEEP_ALIVE) == KEEP_ALIVE);
	}
}