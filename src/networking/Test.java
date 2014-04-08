package networking;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.security.SecureRandom;
import java.util.LinkedList;

import main.Callback;

/**
 * This class will test the functionality of all classes within the package networking
 *
 * @author      Sander Koning <s.koning@student.utwente.nl>
 * @version     0.1
 * @since       2014-04-08
 */
 
public class Test {
	
	@SuppressWarnings("unchecked")
	public Test(){
		System.out.println("--- New Datapacket ---");
		
		DataPacket dp = null;
		try {
			dp = new DataPacket((byte) 12, (byte) 14, (byte) 8, (byte) 16, new Byte[]{0, 5, 19}, true, true, true);
		} catch (DatagramDataSizeException e1) {
			e1.printStackTrace();
		}
		
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
		
		System.out.println("--- Copy Datapacket ---");
		
		DataPacket test = null;
		try {
			test = new DataPacket(dp.getRaw());
		} catch (DatagramDataSizeException e) {
			e.printStackTrace();
		}

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
		System.out.println("--- Networker Tests ---");
		
		try {
			byte[] data = new byte[8];
			(new SecureRandom()).nextBytes(data);
			
			
			LinkedList<DataPacket> packets = new LinkedList<DataPacket>();
			
			Object networker = new Networker(new Callback(this, "getByte"));
			
			Class[] args = new Class[2];
			args[0] = byte.class;
			args[1] = byte[].class;
			
			Method processData = networker.getClass().getDeclaredMethod("processData", args);
			processData.setAccessible(true);
			
			args = new Class[1];
			args[0] = packets.getClass();

			Method processPackets = networker.getClass().getDeclaredMethod("processPackets", args);
			processPackets.setAccessible(true);
			
			packets = (LinkedList<DataPacket>) processData.invoke(networker, (byte) 15, data);
			
			byte[] result = (byte[]) processPackets.invoke(networker, packets);
			
			if(!(stringFromBytes(data).equals(stringFromBytes(result))))
				System.out.println("Packets generating/decoding failed");
			
			System.out.println("Was: " + stringFromBytes(result) + "\nS2B: " + stringFromBytes(data));
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		System.out.println("--- Test Complete ---");
	}
	
	public static void main(String[] args) {
		new Test();
	}
	
	public Byte getByte(){
		return new Byte((byte) 15);
	}
	
	public String stringFromBytes(byte[] b){
		if(b.length == 0) return "";
		
		String poo = new String(Byte.toString(b[0]));
		
		for(int i = 1; i < b.length; i++){
			poo += ", " + Byte.toString(b[i]);
		}
		
		return poo;
	}
}
