/**
 * 
 */
package networking;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
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
		
		if (!packets.containsKey(packet.getSequenceNumber())) {
			packets.put(packet.getSequenceNumber(), packet);
		}
		
		// get last ACK
		byte lAck = this.ACK.get(ackStack);
		while(packets.containsKey(lAck)) {
			lAck++;
		}
		
		lAck--;
		
		this.ACK.put(ackStack, lAck);
		return lAck;		
		
	}
	
	
	
}
