package tests;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.security.SecureRandom;
import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedList;
import java.util.TreeSet;

import routing.LinkStateRouting;
import routing.RouteNotFoundException;
import dijkstra.model.Vertex;
import main.Callback;
import monitoring.NetworkMessage;
import networking.DataPacket;
import networking.DatagramDataSizeException;
import networking.Networker;

public class TestRouting extends TestCase {
	public TestRouting(){
		//TODO Testing
		System.out.println("--- Test Starting ---");
		testNetwork();
		System.out.println("--- Test Complete ---");
	}
	
	@Override
	protected int runTest() {
		// TODO Auto-generated method stub
		testNetwork();
		return 0;
	}

	@Override
	protected void setUp() {
		// TODO Auto-generated method stub
	}
	
	protected void testNetwork() {
		LinkStateRouting r = new LinkStateRouting(null);
		
		Class[] args = new Class[2];
		args[0] = Byte.class;
		args[1] = Byte.class;
		
		Method addLink;
		try {
			addLink = r.getClass().getDeclaredMethod("addPath", args);
			addLink.setAccessible(true);
			
			Method removeLink = r.getClass().getDeclaredMethod("removePath", args);
			removeLink.setAccessible(true);
			
			System.out.println("Creating router");
			System.out.println("Placing routes:");
			
			r.networkMessage((byte)2, NetworkMessage.NEWKEEPALIVE);
			r.addPath((byte)1,(byte)2);
			
			r.networkMessage((byte)3, NetworkMessage.NEWKEEPALIVE);
			r.addPath((byte)2,(byte)3);
			
			r.update();
			
			SimpleEntry<Byte, Byte> route;
			route = r.getRoute((byte)3);
			assertEquals("Route from 1->3: nexthop", route.getKey(), (byte)2);
		} catch (NoSuchMethodException |
					RouteNotFoundException | SecurityException |
					 IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
