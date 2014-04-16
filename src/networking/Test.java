package networking;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
		
		SmallPacket dp = null;
		try {
			dp = new SmallPacket((byte) 12, (byte) 14, (byte) 8, (byte) 16, new byte[]{0, 5, 19}, true, true, true, true);
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
		
		SmallPacket test = null;
		try {
			test = new SmallPacket(dp.getRaw());
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
			byte[] data = new byte[2048];
			(new SecureRandom()).nextBytes(data);
			
			
			LinkedList<SmallPacket> packets = new LinkedList<SmallPacket>();
			
			Object networker = null;
			try {
				networker = new Networker(new Callback(this, "getbyte"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Class[] args = new Class[6];
			args[0] = Byte.class;
			args[1] = Byte.class;
			args[2] = byte[].class;
			args[3] = Boolean.TYPE;
			args[4] = Boolean.TYPE;
			args[5] = Boolean.TYPE;
			
			Method processData = networker.getClass().getDeclaredMethod("processData", args);
			processData.setAccessible(true);
			
			args = new Class[1];
			args[0] = packets.getClass();

			Method processPackets = networker.getClass().getDeclaredMethod("processPackets", args);
			processPackets.setAccessible(true);
			
			packets = (LinkedList<SmallPacket>) processData.invoke(networker, (byte) 15, (byte) 15, data, false, false, false);
			
			byte[] result = (byte[]) processPackets.invoke(networker, packets);
			
			if(!(stringFrombytes(data).equals(stringFrombytes(result))))
				System.out.println("Packets generating/decoding failed");
			
			System.out.println("Was: " + stringFrombytes(result) + "\nS2B: " + stringFrombytes(data));
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}

		System.out.println("--- Test Complete ---");
	}
	
	public static void main(String[] args) {
		new Test();
	}
	
	public byte getbyte(){
		return (byte) 15;
	}
	
	public String stringFrombytes(byte[] b){
		if(b.length == 0) return "";
		
		String poo = new String(Byte.toString(b[0]));
		
		for(int i = 1; i < b.length; i++){
			poo += ", " + Byte.toString(b[i]);
		}
		
		return poo;
	}
}
