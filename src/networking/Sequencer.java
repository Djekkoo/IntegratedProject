/**
 * 
 */
package networking;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import main.Callback;
import main.CallbackException;

/**
 * @author Jacco
 *
 */
public class Sequencer extends Thread{
	private HashMap<Byte, Entry<Byte, Byte>> oneToOne; 	// <node, <sequencenr to, sequencenr from>>
	
	private HashMap<Byte, HashMap<Byte, DataPacket>> packets = new HashMap<Byte, HashMap<Byte, DataPacket>>();
	private HashMap<Byte, Byte> ACK = new HashMap<Byte, Byte>();
	private HashMap<Byte, Byte> RET = new HashMap<Byte, Byte>();
	private HashMap<Byte, Byte> ACKReceived = new HashMap<Byte, Byte>();
	
	private static long checkTimeout = 1000;
	private static int  retransmitThreshold = 2;
	
	// lock
	ReentrantLock lock = new ReentrantLock();
	
	private Callback retransmit;
	
	public Sequencer(Callback retransmit){
		this.retransmit = retransmit;
		
		this.oneToOne = new HashMap<Byte, Entry<Byte, Byte>>();
		
		this.start();
		
	}

	public boolean isAccessible(Byte source) {
		if (this.oneToOne.containsKey(source) == false)
			return false;
		
		if (this.oneToOne.get(source).getValue() == (byte)0)
			return false;
					
		if (this.oneToOne.get(source).getKey() == (byte) 0)
			return false;
		
		return true;
		
	}
	
	public void setSequenceFrom(Byte source, Byte sequence) {
		this.lock.lock();
		this.packets.put(source, new HashMap<Byte, DataPacket>());
		if(this.oneToOne.containsKey(source)) {
			this.oneToOne.put(source, new SimpleEntry<Byte, Byte>(this.oneToOne.get(source).getKey(),sequence));
		} else {
			this.oneToOne.put(source, new SimpleEntry<Byte, Byte>((byte)0,sequence));
		}
		this.ACK.put(source, this.prevSEQ(sequence));
		this.RET.put(source, this.prevSEQ(sequence));
		this.lock.unlock();
	}
	
	public void setSequenceTo(Byte source, Byte sequence) {
		this.lock.lock();
		if(this.oneToOne.containsKey(source)) {
			this.oneToOne.put(source, new SimpleEntry<Byte, Byte>(sequence, this.oneToOne.get(source).getValue()));
		} else {
			this.oneToOne.put(source, new SimpleEntry<Byte, Byte>(sequence, (byte)0));
		}
		this.ACKReceived.put(source, this.prevSEQ(sequence));
		this.lock.unlock();
	}
	
	// check for ACK's waiting too long
	public void run() {
		
		Iterator<Byte> iter;
		Byte tAck;
		byte rStack;
		boolean resend = false;
		
		while (true) {
			
			this.lock.lock();
			
			iter = this.oneToOne.keySet().iterator(); 
			while(iter.hasNext()) {
				rStack = iter.next();
				resend = true;
				tAck = this.ACKReceived.get(rStack);
				
				byte temp = 0;
				if(tAck != null){
					temp = tAck;
				} else {
					System.out.println("tAck == null");
					continue;
				}
				
				for (int i = 0; i < retransmitThreshold; i++) {
					if ((byte) tAck == (byte)this.oneToOne.get(rStack).getKey()) {
						resend = true;
						break;
					}
					tAck = this.nextSEQ(tAck);
				}
				
				if (resend == false && (byte) tAck != (byte) this.oneToOne.get(rStack).getKey()) {
					try {
						this.retransmit.invoke(Byte.valueOf(rStack), Byte.valueOf(this.nextSEQ(temp)));
					} catch (CallbackException e) {
						System.out.println("Error retransmitting!!");
						e.printStackTrace();
					}
				}
				
			}
			
			this.lock.unlock();
			try {
				Thread.sleep(checkTimeout);
			} catch (InterruptedException e) {}
			
		}
		
	}
	
	public Byte getTo(Byte node){
		lock.lock();

		if (this.oneToOne.containsKey(node) == false || this.oneToOne.get(node).getKey() == (byte)0) {
			System.out.println("No sequence available, ACK's are not registered");
			lock.unlock();
			return null;
		}
		SimpleEntry<Byte, Byte> e = (SimpleEntry<Byte, Byte>) oneToOne.get(node);
		byte b = e.getKey();
		e = new SimpleEntry<Byte, Byte>(this.nextSEQ(b), e.getValue());
		oneToOne.put(node, e);
		lock.unlock();
		return b;
	}
	
	public LinkedList<DataPacket> getPackets(Byte source) {
		
		byte rStack = source;
		LinkedList<DataPacket> res = new LinkedList<DataPacket>();
		
		this.lock.lock();
		
		HashMap<Byte, DataPacket> packets = new HashMap<Byte, DataPacket>();
		if (this.packets.containsKey(rStack))
			packets.putAll(this.packets.get(rStack));
		
		
		byte bRet = this.RET.get(rStack);
		byte temp = 0x00;
		LinkedList<DataPacket> tList = new LinkedList<DataPacket>();
		
		while(packets.containsKey(this.nextSEQ(bRet))) {
			
			bRet = this.nextSEQ(bRet);
			
			if (!packets.get(bRet).hasMore()) {
				
				// End of multi-packet data
				if (temp != (byte) 0x00) {
					temp = 0x00;

					tList.add(packets.get(bRet));
					
					Iterator<DataPacket> i = tList.iterator();
					while(i.hasNext()) {
						DataPacket dp = i.next();
						res.add(dp);
						this.packets.get(rStack).remove(dp.getSequenceNumber());
					}
					
					tList = new LinkedList<DataPacket>();
					
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
			
		}

		if (temp != (byte) 0x00) {
			bRet = this.prevSEQ(temp);
		}
		
		this.RET.put(rStack, bRet);
		
		this.lock.unlock();
		
		return res;
		
		
	}
	
	// returns ACK-number
	public Byte put(DataPacket packet) {
		
		byte ackStack = 0x00;
		ackStack |= packet.getSource();
		
		this.lock.lock();
		
		//
		if (this.oneToOne.containsKey(ackStack) == false/* || this.oneToOne.get(ackStack).getValue() == (byte)0*/ || this.oneToOne.get(ackStack).getValue() == (byte) 0) {
			
			System.out.println("Dropped packet, ACK's are not registered");
			this.lock.unlock();
			return null;
		}
		
		// ACK?
		if (packet.isAck()) {
			
			this.ACKReceived.put(ackStack, packet.getSequenceNumber());
			System.out.println("RECEIVED ACK: " + ackStack + ":" + packet.getSequenceNumber());
			this.lock.unlock();
			return null;
			
		} 
		
		if (!this.packets.containsKey(ackStack)) {
			this.packets.put(ackStack, new HashMap<Byte, DataPacket>());
		}
		
		if ((this.ACK.get(ackStack) < packet.getSequenceNumber() && packet.getSequenceNumber() - this.ACK.get(ackStack) >= 127)
		 ||(this.ACK.get(ackStack) > packet.getSequenceNumber() && (256-this.ACK.get(ackStack) + packet.getSequenceNumber() - 1) >= 127)
			) {
			
			System.out.println("Packet dropped, queue too long");
			
		} else {
			
			HashMap<Byte, DataPacket> packets = this.packets.get(ackStack);
			packets.put(packet.getSequenceNumber(), packet);
			System.out.println("Package received with seq="+packet.getSequenceNumber());
			this.packets.put(ackStack, packets);
		}
		
		// get last ACK
		byte lAck = this.ACK.get(ackStack);
		while(packets.get(ackStack).containsKey(this.nextSEQ(lAck))) {
			lAck = this.nextSEQ(lAck);
		} 
		
		this.ACK.put(ackStack, lAck);
		
		this.lock.unlock();
		
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