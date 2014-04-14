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
	}
	
	@Override
	protected int runTest() {
		// TODO Auto-generated method stub
		testNetwork();
		System.out.println("--- Test Complete ---");
		return 0;
	}

	@Override
	protected void setUp() {
		// TODO Auto-generated method stub
	}
	
	protected void testNetwork() {
		LinkStateRouting r = new LinkStateRouting(null,null,false);
		try {
			System.out.println("Creating router...");
			System.out.println("Placing routes...");
			System.out.print("Running tests: ");
			
			r.networkMessage((byte)2, NetworkMessage.NEWKEEPALIVE);
			
			r.addNode((byte)3);
			r.addPath((byte)2,(byte)3);
			
			r.addNode((byte)4);
			r.addPath((byte)3,(byte)4);
			
			r.update();
			System.out.print("1-");
			assertEquals("Route 1->2->3->4: nexthop", (byte)2, r.getRoute((byte)4).getKey());
			System.out.print("2-");
			assertEquals("Max routing distance: ", r.getLongestRoute(), (byte)2);
			
			r.networkMessage((byte)5, NetworkMessage.NEWKEEPALIVE);
			r.addPath((byte)4,(byte)5);
			
			r.update();
			System.out.print("3-");
			assertEquals("Route 1->5->3: nexthop", (byte)5, r.getRoute((byte)4).getKey());
			System.out.print("4-");
			assertEquals("Max routing distance: ", (byte)1, r.getLongestRoute());
			
			r.removeNode((byte)5);
			r.update();
			System.out.print("5-");
			assertEquals("Route 1->2->3->4: nexthop", (byte)2, r.getRoute((byte)4).getKey());
			System.out.print("6-");
			assertEquals("Max routing distance: ", (byte)2, r.getLongestRoute());
			
			System.out.println("7");
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
