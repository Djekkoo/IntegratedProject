package routing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.security.SecureRandom;
import java.util.LinkedList;

import tests.TestCase;
import main.Callback;
import networking.DataPacket;
import networking.DatagramDataSizeException;
import networking.Networker;

public class Test {
	public Test(){
		/*System.out.println("--- Networker Tests ---");
		
		TestCase tester = new TestCase();
		try {
			
		}
		
		try {
			byte[] data = new byte[2048];
			(new SecureRandom()).nextBytes(data);
			
			
			LinkedList<DataPacket> packets = new LinkedList<DataPacket>();
			
			Object networker = new Networker(new Callback(this, "getbyte"));
			
			Class[] args = new Class[2];
			args[0] = Byte.class;
			args[1] = byte[].class;
			
			Method processData = networker.getClass().getDeclaredMethod("processData", args);
			processData.setAccessible(true);
			
			args = new Class[1];
			args[0] = packets.getClass();

			Method processPackets = networker.getClass().getDeclaredMethod("processPackets", args);
			processPackets.setAccessible(true);
			
			packets = (LinkedList<DataPacket>) processData.invoke(networker, (byte) 15, data);
			
			byte[] result = (byte[]) processPackets.invoke(networker, packets);
			
			if(!(stringFrombytes(data).equals(stringFrombytes(result))))
				System.out.println("Packets generating/decoding failed");
			
			System.out.println("Was: " + stringFrombytes(result) + "\nS2B: " + stringFrombytes(data));
		} catch (SocketException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}

		System.out.println("--- Test Complete ---");*/
	}
}
