package tests;

import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import routing.LinkStateRouting;
import routing.RouteNotFoundException;
import monitoring.NetworkMessage;

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
		LinkStateRouting r = new LinkStateRouting(null,false);
		try {
			System.out.println("Creating router");
			System.out.println("Placing routes");
			
			r.networkMessage((byte)2, NetworkMessage.NEWKEEPALIVE);
			
			r.addNode((byte)3);
			r.addPath((byte)2,(byte)3);
			
			r.addNode((byte)4);
			r.addPath((byte)3,(byte)4);
			
			r.update();
			assertEquals("Route 1->2->3->4: nexthop", r.getRoute((byte)4).getKey(), (byte)2);
			//assertEquals("Max routing distance: ", r.getLongestRoute(), (byte)2);
			
			r.networkMessage((byte)5, NetworkMessage.NEWKEEPALIVE);
			r.addPath((byte)4,(byte)5);
			
			r.update();
			assertEquals("Route 1->5->3: nexthop", r.getRoute((byte)4).getKey(), (byte)5);
			assertEquals("Max routing distance: ", r.getLongestRoute(), (byte)2);
			
			r.removeNode((byte)5);
			r.update();
			assertEquals("Route 1->2->3->4: nexthop", r.getRoute((byte)4).getKey(), (byte)2);
			
			try {
				r.removeNode((byte)3);
				r.update();
				r.getRoute((byte)4).getKey();
				System.out.println("FAILED: Route found from 1 to 5");
			} catch (RouteNotFoundException e) {
			}

			
		} catch (RouteNotFoundException | SecurityException |
					 IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
