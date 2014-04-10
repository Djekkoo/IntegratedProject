/**
 * 
 */
package networking;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map.Entry;

import main.IntegrationProject;

/**
 * @author sander
 *
 */
public class Sequencer {
	private HashMap<Byte, Byte> broadcasts; 			// <node, sequencenr>
	private HashMap<Byte, Entry<Byte, Byte>> oneonone; 	// <node, <sequencenr to, sequencenr from>>
	
	
	
	public Sequencer(){
		broadcasts = new HashMap<Byte, Byte>();
		broadcasts.put(IntegrationProject.DEVICE, (byte) 0);
		
		oneonone = new HashMap<Byte, Entry<Byte, Byte>>();
		oneonone.put((byte) 0x0F, new SimpleEntry<Byte, Byte>((byte) 0, (byte) 0));
	}
	
	public Byte getBroadcast(){
		return getTo((byte) 0x0F);
	}
	
	public Byte getTo(Byte node){
		SimpleEntry<Byte, Byte> e = (SimpleEntry<Byte, Byte>) oneonone.get(node);
		byte b = e.getKey();
		e = new SimpleEntry<Byte, Byte>((byte) (b + 1), e.getValue());
		oneonone.put(node, e);
		return b;
	}
	
	
}
