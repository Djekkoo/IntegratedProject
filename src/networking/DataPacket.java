package networking;

public class DataPacket{
	
	public static final int HEADER_LENGTH = 4;
	
	private static final byte ACK = (byte) 0x10;
	private static final byte ROUTING = (byte) 0x20;
	private static final byte KEEP_ALIVE = (byte) 0x40;
	
	private byte[] packet = new byte[0];
	
	public DataPacket(byte[] raw) throws DatagramDataSizeException{
		if (raw.length < HEADER_LENGTH) throw new DatagramDataSizeException(raw.length);
		
		//TODO: Moar checks plzz
		
		packet = raw;
	}

	public DataPacket(byte source, byte destination, byte hops, byte sequencenr, byte[] data, boolean ack, boolean routing, boolean keepalive) throws DatagramDataSizeException{

		if(data.length > (1024 - HEADER_LENGTH)) throw new DatagramDataSizeException(data.length + HEADER_LENGTH);
		
		packet = new byte[data.length + HEADER_LENGTH];
		
		System.arraycopy(data, 0, packet, HEADER_LENGTH, data.length);
		
		packet[0] = source;
		packet[1] = destination;
		
		if(hops > 0x0F) hops = 0x0F;
		
		packet[2] = (byte) (hops & 0x0F);

		if(ack) 		packet[2] |= ACK;
		if(routing) 	packet[2] |= ROUTING;
		if(keepalive) 	packet[2] |= KEEP_ALIVE;
		
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
	
	public static void main(String[] args) {
		DataPacket dp = null;
		try {
			dp = new DataPacket((byte) 12, (byte) 14, (byte) 8, (byte) 16, new byte[]{0, 5, 19}, true, true, true);
		} catch (DatagramDataSizeException e1) {
			e1.printStackTrace();
		}
		
		System.out.println("--- New Datapacket ---");

		if(!dp.isAck()) 		System.out.println("Faulty isAck state");
		if(!dp.isRouting()) 	System.out.println("Faulty isRouting state");
		if(!dp.isKeepAlive()) 	System.out.println("Faulty isKeepAlive state");
		
		if(!new String(dp.getData()).equals(new String(new byte[]{0, 5, 19})))
								System.out.println("Faulty data stored");
		
		if(!(dp.getSequenceNumber() == (byte) 16))
								System.out.println("Faulty sequencenumber stored");
		if(!(dp.getHops() == (byte) 8))
								System.out.println("Faulty hops stored");
		
		if(!(dp.getSource() == (byte) 12))
								System.out.println("Faulty source stored");
		
		if(!(dp.getDestination() == (byte) 14))
								System.out.println("Faulty destination stored");
		
		System.out.println("--- Test Complete ---");
		
		DataPacket test = null;
		try {
			test = new DataPacket(dp.getRaw());
		} catch (DatagramDataSizeException e) {
			e.printStackTrace();
		}
		
		System.out.println("--- Copy Datapacket ---");

		if(!test.isAck()) 		System.out.println("Faulty isAck state");
		if(!test.isRouting()) 	System.out.println("Faulty isRouting state");
		if(!test.isKeepAlive()) 	System.out.println("Faulty isKeepAlive state");
		
		if(!new String(test.getData()).equals(new String(new byte[]{0, 5, 19})))
								System.out.println("Faulty data stored");
		
		if(!(test.getSequenceNumber() == (byte) 16))
								System.out.println("Faulty sequencenumber stored");
		if(!(test.getHops() == (byte) 8))
								System.out.println("Faulty hops stored");
		
		if(!(test.getSource() == (byte) 12))
								System.out.println("Faulty source stored");
		
		if(!(test.getDestination() == (byte) 14))
								System.out.println("Faulty destination stored");
		
		System.out.println("--- Test Complete ---");
	}
}