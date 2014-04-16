package networking;

public interface DataPacket {
	/*
	 * Setters
	 */
	
	/**
	 * Decreases hops with a minimum of 0
	 */
	
	public void decreaseHops();
	
	/*
	 * Getters
	 */
	
	/**
	 * 
	 * @return Array of bytes with data and headers
	 */
	public byte[] getRaw();
	
	/**
	 * 
	 * @return Source
	 */
	public byte getSource();
	
	/**
	 * 
	 * @return Destination
	 */
	public byte getDestination();
	
	/**
	 * 
	 * @return Array of bytes with data without no headers
	 */
	public byte[] getData();
	
	/**
	 * 
	 * @return Hops
	 */
	public byte getHops();
	
	/**
	 * 
	 * @return Sequencenumber
	 */
	public byte getSequenceNumber();
	
	/**
	 * 
	 * @return is an ACK
	 */
	public boolean isAck();
	
	/**
	 * 
	 * @return is used for routing protocol
	 */
	public boolean isRouting();
	
	/**
	 * 
	 * @return is used as a keep alive signal
	 */
	public boolean isKeepAlive();
	
	/**
	 * 
	 * @return is fragmented and not the last packet
	 */
	public boolean hasMore();
}
