package tests;

import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import routing.LinkStateRouting;
import routing.RouteNotFoundException;
import monitoring.NetworkMessage;

public class TestRouting extends TestCase {
	public TestRouting(){
		//TODO Testing
	}
	
	@Override
	protected int runTest() {
		// TODO Auto-generated method stub
		
		return testNetwork();
	}

	@Override
	protected void setUp() {
		// TODO Auto-generated method stub
	}
	
	protected int testNetwork() {
		LinkStateRouting r = new LinkStateRouting(null,null,false);
		try {
			System.out.println("Creating router");
			System.out.println("Placing routes");
			System.out.println("Running tests");
			
			r.networkMessage((byte)2, NetworkMessage.NEWKEEPALIVE);
			
			r.addNode((byte)3);
			r.addPath((byte)2,(byte)3);
			
			r.addNode((byte)4);
			r.addPath((byte)3,(byte)4);
			
			r.update();
			assertEquals("Route 1->2->3->4: nexthop 1", (byte)2, r.getRoute((byte)4).getKey());
			assertEquals("Max routing distance: ", r.getLongestRoute(), (byte)2);
			
			r.networkMessage((byte)5, NetworkMessage.NEWKEEPALIVE);
			r.addPath((byte)4,(byte)5);
			
			r.update();
			assertEquals("Route 1->5->4: nexthop", (byte)5, r.getRoute((byte)4).getKey());
			assertEquals("Route 1->5: nexthop", (byte)5, r.getRoute((byte)5).getKey());
			assertEquals("Max routing distance: ", (byte)1, r.getLongestRoute());
			
			r.removeNode((byte)5);
			r.update();
			
			assertEquals("Route 1->2->3->4: nexthop #2", (byte)2, r.getRoute((byte)4).getKey());
			assertEquals("Max routing distance: ", (byte)2, r.getLongestRoute());
			
			try {
				r.removeNode((byte)3);
				r.update();
				r.getRoute((byte)4).getKey();
				System.out.println("FAILED: Route found from 1 to 5");
			} catch (RouteNotFoundException e) {
			}
			
			r.addNode((byte)3);
			r.addPath((byte)3, (byte)1);
			r.addNode((byte)5);
			r.addPath((byte)5, (byte)2);
			
			r.addNode((byte)6);
			r.addPath((byte)5, (byte)6);
			r.addPath((byte)6, (byte)3);
			r.addPath((byte)3, (byte)1);
			
			r.update();

			
			assertEquals("Route 1->2->5: nexthop", (byte)2, r.getRoute((byte)5).getKey());

			r.networkMessage((byte)2, NetworkMessage.NOKEEPALIVE);
			r.update();
			assertEquals("Route 1->3->6->5: nexthop", (byte)3, r.getRoute((byte)5).getKey());

		} catch (RouteNotFoundException | SecurityException |
					 IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return errors;
	}
}
