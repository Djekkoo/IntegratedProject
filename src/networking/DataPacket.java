package networking;

public class DataPacket{
	
	private static final byte ACK = (byte) 0x10;
	private static final byte ROUTING = (byte) 0x20;
	
	private byte[] packet = new byte[1024];

	public DataPacket(byte source, byte destination, byte hops, byte sequence, byte[] data, boolean ack, boolean routing){
		packet[0] = source;
		packet[1] = destination;
		
		if(hops > 0x0F) hops = 0x0F;
		
		packet[3] = hops;

		if(ack) 	packet[3] |= ACK;
		if(routing) packet[3] |= ROUTING;
		
		packet[4] = sequence;
		
		if(data.length <= (1024 - 8))
			System.arraycopy(data, 0, packet, 9, data.length);
	}
}