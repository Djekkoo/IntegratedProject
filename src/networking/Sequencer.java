/**
 * 
 */
package networking;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import main.IntegrationProject;

/**
 * @author sander
 *
 */
public class Sequencer {
	private HashMap<Byte, Byte> broadcasts; 			// <node, sequencenr>
	private HashMap<Byte, Entry<Byte, Byte>> oneToOne; 	// <node, <sequencenr to, sequencenr from>>
	
	private HashMap<Byte, HashMap<Byte, DataPacket>> packets = new HashMap<Byte, HashMap<Byte, DataPacket>>();
	private HashMap<Byte, Byte> ACK = new HashMap<Byte, Byte>();
	private HashMap<Byte, Byte> RET = new HashMap<Byte, Byte>();
	
	public Sequencer(){
		broadcasts = new HashMap<Byte, Byte>();
		broadcasts.put(IntegrationProject.DEVICE, (byte) 0);
		
		oneToOne = new HashMap<Byte, Entry<Byte, Byte>>();
		oneToOne.put((byte) 0x0F, new SimpleEntry<Byte, Byte>((byte) 0, (byte) 0));
	}
	
	public Byte getBroadcast(){
		return getTo((byte) 0x0F);
	}
	
	public Byte getTo(Byte node){
		SimpleEntry<Byte, Byte> e = (SimpleEntry<Byte, Byte>) oneToOne.get(node);
		byte b = e.getKey();
		e = new SimpleEntry<Byte, Byte>((byte) (b + 1), e.getValue());
		oneToOne.put(node, e);
		return b;
	}
	
	public LinkedList<DataPacket> getPackets(Byte source, Boolean broadcast) {
		
		byte rStack = 0x00;
		LinkedList<DataPacket> res = new LinkedList<DataPacket>();
		
		if (broadcast.equals(Boolean.TRUE)){
			rStack |= 0xF0;
		}
		rStack |= source;
		
		HashMap<Byte, DataPacket> packets = new HashMap<Byte, DataPacket>();
		packets.putAll(this.packets.get(rStack));
		
		
		byte bRet = this.RET.get(rStack);
		byte temp = 0x00;
		LinkedList<DataPacket> tList = new LinkedList<DataPacket>();
		
		while(packets.containsKey(bRet)) {
			
			if (!packets.get(bRet).hasMore()) {
				
				// End of multi-packet data
				if (temp != (byte) 0x00) {
					temp = 0x00;
					
					Iterator<DataPacket> i = tList.iterator();
					while(i.hasNext()) {
						DataPacket dp = i.next();
						res.add(dp);
						this.packets.get(rStack).remove(dp.getSequenceNumber());
					}
					
					tList = new LinkedList<DataPacket>();
					bRet = this.nextSEQ(bRet);
					
					continue;
				}
				
				// single packet data
				res.add(packets.get(bRet));
				this.packets.get(rStack).remove(bRet);
				
			// multi-packet data
			} else {
				
				if (temp == (byte) 0x00)
					temp = bRet;
				
				tList.add(packets.get(bRet));
				
			}
			
			bRet = this.nextSEQ(bRet);
			
		}

		if (temp != (byte) 0x00) {
			bRet = temp;
		}
		
		this.RET.put(rStack, this.prevSEQ(bRet));
		return res;
		
		
	}
	
	// returns ACK-number
	public Byte put(DataPacket packet) {
		
		byte ackStack = 0x00;
		if (packet.getDestination() == (byte) 0x0F) {
			ackStack |= 0xF0;
		}
		
		ackStack |= packet.getSource();
		if (!this.packets.containsKey(ackStack)) {
			this.packets.put(ackStack, new HashMap<Byte, DataPacket>());
			this.ACK.put(ackStack, (byte) 0);
			this.RET.put(ackStack, (byte) 0);
		}
		HashMap<Byte, DataPacket> packets = this.packets.get(ackStack);
		packets.put(packet.getSequenceNumber(), packet);
		
		// get last ACK
		byte lAck = this.ACK.get(ackStack);
		while(packets.containsKey(lAck)) {
			lAck = this.nextSEQ(lAck);
		}
		
		lAck = this.prevSEQ(lAck);
		
		this.ACK.put(ackStack, lAck);
		return lAck;
		
	}

	private byte nextSEQ(byte seq) {
		
		if (seq == (byte)0xFF)
			return (byte) 0x01;
		
		return (byte) (seq+1);
		
	}

	private byte prevSEQ(byte seq) {
		
		if (seq == (byte)0x01)
			return (byte) 0xFF;
		
		return (byte) (seq-1);
		
	}
	
	
	
}
