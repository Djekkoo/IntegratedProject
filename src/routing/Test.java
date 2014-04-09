package routing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.TreeSet;

import dijkstra.model.Vertex;
import tests.TestCase;
import main.Callback;
import networking.DataPacket;
import networking.DatagramDataSizeException;
import networking.Networker;

public class Test {
	public Test(){
		//TODO Testing
		/*
		TreeMap<Byte,TreeSet<Byte>> nw = new TreeMap<Byte,TreeSet<Byte>>();
		TreeSet<Byte> t = new TreeSet<Byte>();
		t.add((byte)2);
		t.add((byte)3);
		nw.put((byte)1,t);
		
		t = new TreeSet<Byte>();
		t.add((byte)1);
		t.add((byte)3);
		nw.put((byte)2,t);
		
		t = new TreeSet<Byte>();
		t.add((byte)1);
		t.add((byte)2);
		t.add((byte)4);
		nw.put((byte)3,t);
		
		t = new TreeSet<Byte>();
		t.add((byte)3);
		t.add((byte)5);
		nw.put((byte)4,t);
		
		t = new TreeSet<Byte>();
		t.add((byte)4);
		nw.put((byte)5,t);
		
		LinkedList<Vertex> path = findPath((byte)1,(byte)5);
		for(Vertex v : path) {
			System.out.print(v.getId() + " -> ");
		}
		System.out.println("Done!");
		
		nw.get((byte)3).remove((byte)1);
		nw.get((byte)1).remove((byte)3);
		nw.get((byte)3).remove((byte)4);
		nw.get((byte)4).remove((byte)3);
		
		path = findPath((byte)1,(byte)5);
		if(path == null) {
			System.out.println("1 -> NO ROUTE FOUND");
		} else {
			for(Vertex v : path) {
				System.out.print(v.getId() + " -> ");
			}
			System.out.println("Done!");
		}
		
		nw.get((byte)1).add((byte)6);
		nw.get((byte)5).add((byte)6);
		
		t = new TreeSet<Byte>();
		t.add((byte)1);
		t.add((byte)5);
		nw.put((byte)6,t);
		
		path = findPath((byte)1,(byte)5);
		for(Vertex v : path) {
			System.out.print(v.getId() + " -> ");
		}
		System.out.println("Done!");
		
		System.out.println("--- Networker Tests ---");
		
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
