/**
 * 
 */
package routing;

import java.nio.ByteBuffer;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import dijkstra.DijkstraAlgorithm;
import dijkstra.model.*;
import networking.DataPacket;
import main.Callback;
import main.CallbackException;
import main.IntegrationProject;
import monitoring.NetworkMessage;

/**
 * Implements a Link-State routing protocol to explore the network,
 * then uses pathfinding to find the shortest paths.
 * 
 * @author      Joey Haas <j.haas@student.utwente.nl>
 * @version     0.1
 * @since       2014-04-07
 */
public class LinkStateRouting implements RoutingInterface {
	
	private TreeMap<Byte,TreeSet<Byte>> nw = new TreeMap<Byte,TreeSet<Byte>>();
	private long lastReceivedPacket = 0;
	private long lastSentPacket = 0;
	private Callback send;
	private byte DEVICE;
	
	private Vertex[] vertexArray;
	
	private HashMap<Byte,Byte> nextHops = new HashMap<Byte,Byte>();
	private HashMap<Byte,Byte> routeLens = new HashMap<Byte,Byte>();
	
	private DijkstraAlgorithm pathFinder;
	
	public LinkStateRouting(Callback send) {
		this.send = send;
		this.DEVICE = IntegrationProject.DEVICE;
		
		nw.put(DEVICE, new TreeSet<Byte>());
		
		update();
		
	}
	
	public static void main(String[] args) {
		RoutingInterface r = new LinkStateRouting(null);
	}
	
	//PUBLIC
	
	/**
	 * {@inheritDoc}
	 */
	public void packetReceived(DataPacket p) {
		// TODO Handle packet receives
		boolean updated = parsePacket(p.getData());
		if(updated) {
			sendToNeighbours(buildPacket());
		}
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	//SimpleEntry: NextHop, Distance
	public SimpleEntry<Byte, Byte> getRoute(Byte destination)
			throws RouteNotFoundException {
		// TODO Respond whenever a route is requested.
		if(!nw.containsKey(destination))
			throw new RouteNotFoundException("Destination unknown.");
		else if(nw.get(destination).isEmpty() || nw.get(DEVICE).isEmpty())
			throw new RouteNotFoundException("Destination unreachable; no route to host.");
		
		Byte nextHop = nextHops.get(destination);
		Byte routeLen = routeLens.get(destination);
		if(nextHop == -1 || routeLen == -1) {
			update();
			throw new RouteNotFoundException("Destination unreachable; no route to host.");
		}
		
		return new SimpleEntry<Byte,Byte>(nextHop,routeLen);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void networkMessage(Byte node, NetworkMessage type) {
		// TODO Handle messages
		try{
			switch(type) {
			case NEWKEEPALIVE:
				System.out.println("New user!");
				nw.get(DEVICE).add(node);
				send.invoke(node,buildPacket());
				update();
				break;
			case DROPPED:
				nw.get(DEVICE).remove(node);
				update();
				break;
			case NOKEEPALIVE:
				update();
				break;
			default:
				break;
			}
		} catch(CallbackException e) {
			e.printStackTrace();
		}
	}
	
	public Boolean isReachable(Byte node) {
		return (findPath(DEVICE,node) == null);
	}
	
	public Byte getLongestRoute() {
		Byte max = 0;
		for(Entry<Byte,Byte> e : routeLens.entrySet()) {
			Byte nextHop = e.getValue();
			if(nextHop > max) {
				max = nextHop;
			}
		}
		return max;
	}
	
	// PRIVATE
	
	private LinkedList<Vertex> findPath(Byte src, Byte dst) {
		DijkstraAlgorithm pf;
		LinkedList<Vertex> path;
		pf = getPathFinder();
		
		if(vertexArray[src] != null && vertexArray[dst] != null) {
			pf.execute(vertexArray[src]);
			path = pf.getPath(vertexArray[dst]);
			return path;
		}
		return null;
	}
	
	private HashMap<Byte,LinkedList<Vertex>> findAllPaths() {
		DijkstraAlgorithm pf;
		HashMap<Byte, LinkedList<Vertex>> paths = new HashMap<Byte,LinkedList<Vertex>>();
		pf = getPathFinder();
		pf.execute(vertexArray[DEVICE]);
		for(Vertex v : vertexArray) {
			if(v != null) {
				Byte vID = Byte.parseByte(v.getId());
				paths.put(vID,pf.getPath(v));
			}
		}
		return paths;
	}
	
	public void addPath(Byte A, Byte B) {
		nw.get((byte)A).add((byte)B);
		nw.get((byte)B).add((byte)A);
	}
	
	public void removePath(Byte A, Byte B) {
		nw.get((byte)A).remove((byte)B);
		nw.get((byte)B).remove((byte)A);
	}
	
	private void update() {
		HashMap<Byte,LinkedList<Vertex>> paths = findAllPaths();
		nextHops.clear();
		routeLens.clear();
		for(Entry<Byte,LinkedList<Vertex>> path : paths.entrySet()) {
			Byte id = path.getKey();
			if(path.getValue() != null) {
				Byte nextHop = Byte.parseByte(path.getValue().get(1).getId());
				nextHops.put(id,nextHop);
				routeLens.put(id,(byte)(path.getValue().size()-2));
			} else {
				nextHops.put(id,(byte)(-1));
				routeLens.put(id,(byte)(-1));
			}
		}
	}
	
	private DijkstraAlgorithm getPathFinder() {
		List<Vertex> vertices = new ArrayList<Vertex>();
		List<Edge> edges = new ArrayList<Edge>();
		
		this.vertexArray = new Vertex[16];
		
		for(Entry<Byte,TreeSet<Byte>> neighbour : nw.entrySet()) {
			String id = neighbour.getKey().toString();
			Vertex v = new Vertex(id,id);
			vertices.add(v);
			vertexArray[neighbour.getKey()] = v;
		}
		for(Entry<Byte,TreeSet<Byte>> neighbour : nw.entrySet()) {
			for(Byte b : neighbour.getValue()) {
				String name = b.toString() + neighbour.getKey().toString();
				edges.add(new Edge(name,vertexArray[neighbour.getKey()],vertexArray[b],1));
			}
		}
		Graph network = new Graph(vertices, edges);
		DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(network);
		return dijkstra;
	}
	
	private void sendToNeighbours(Byte[] data) {
		try {
			TreeSet<Byte> neighbours = nw.get(DEVICE);
			for(Byte nb : neighbours) {
				send.invoke(nb,toByteArray(data));
			}
		} catch (CallbackException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Builds the routing packet to be sent to the neighbours.
	 * Done by serializing the network data and adding metadata;
	 * 
	 * @param	p	The bytes of the packet to be parsed.
	 * @return 	True if the new packet caused the network tree to update,
	 * @return	False if no changes were made.
	 * @since	2014-04-08
	 */
	private boolean parsePacket(byte[] p) {
		//Retrieve the timestamp from the packet
		long timestamp = 0;
	    ByteBuffer buffer = ByteBuffer.wrap(p,0,8);
	    timestamp = buffer.getLong();
		boolean updated = false;
		
		//Is the packet newer than the last received packet?
		if(timestamp > lastReceivedPacket && timestamp > lastSentPacket) {
			lastReceivedPacket = timestamp;
			//Get how many hosts there are in the data
			int numHosts = p[8];
			int offset = 9;
			for(int i = 0; i < numHosts; i++) {
				//Get the host of the entry
				byte host = p[0+offset];
				//Get how many neighbours are in the entry
				int numNeighbours = p[1+offset];
				byte[] neighbours = new byte[numNeighbours];
				System.arraycopy(p, 2+offset, neighbours, 0, numNeighbours);
				offset += numNeighbours+2;
				
				TreeSet<Byte> oldNeighbours = new TreeSet<Byte>();
				for(Object nb : nw.get(host).toArray()) {
					oldNeighbours.add((Byte)nb);
				}
				
				for(byte nb : neighbours) {
					//Do we have a host that we have no record of?
					if(nw.containsKey(host)) {
						nw.get(host).add(nb);
						oldNeighbours.remove(nb);
					} else {
						nw.put(host, new TreeSet<Byte>());
						nw.get(host).add(nb);
						oldNeighbours.remove(nb);
						updated = true;
					}
				}
				for(Byte nb : oldNeighbours) {
					nw.remove(nb);
					updated = true;
				}
			}
		}
		return updated;
	}
	
	/**
	 * Builds the routing packet to be sent to the neighbours.
	 * Done by serializing the network data and adding metadata;
	 * 
	 * @return 	The Byte array containing the serialized network data and metadata.
	 * @since	2014-04-08
	 */
	private Byte[] buildPacket() {
		ArrayList<Byte> p = new ArrayList<Byte>();
		
		// Add timestamp to the first 8 bits.
		long timestamp = new Date().getTime();
		lastSentPacket = timestamp;
		byte[] timestampBytes = ByteBuffer.allocate(8).putLong(timestamp).array();
		for(byte b : timestampBytes) {
			p.add((Byte)b);
		}
		
		// Add the host count.
		p.add(Byte.valueOf((byte)(nw.size()&0xFF)));
		for (Entry<Byte,TreeSet<Byte>> e : nw.entrySet()) {
			Byte host = e.getKey();
			TreeSet<Byte> neighbours = e.getValue();
			p.add(host);
			p.add(Byte.valueOf((byte)(neighbours.size()&0xFF)));
			for(Byte b : neighbours) {
				p.add(b);
			}
		}
		
		return toByteObjectArray(p);
	}
	
	/**
	 * Prints the nodes and prints what nodes they are connected to.
	 * 
	 * @since	2014-04-08
	 */
	
	private void showNetwork() {
		for(Entry<Byte,TreeSet<Byte>> e : nw.entrySet()) {
			System.out.println(e.getKey() + " to: ");
			for(Byte b : e.getValue()) {
				System.out.println("\t" + b);
			}
		}
	}
	
	private Byte[] toByteObjectArray(ArrayList<Byte> bytes) {
		//ArrayList<Byte> into Byte[]
		Byte[] byteObjects = new Byte[bytes.size()];
		int i = 0;
		for(Object b : bytes) {
			Byte bObj = (Byte)b;
			byteObjects[i++] = bObj;
		}
		return byteObjects;
	}
	
	private Byte[] toByteObjectArray(byte[] bytes) {
		//A really hacky way to cast byte[] to Byte[]
		Byte[] byteObjects = new Byte[bytes.length];
		int i = 0;
		for(byte b : bytes) {
			Byte bObj = (Byte)b;
			byteObjects[i++] = bObj;
		}
		return byteObjects;
	}
	
	private byte[] toByteArray(Byte[] byteObjects) {
		//A really hacky way to cast Byte[] to byte[]
		byte[] bytes = new byte[byteObjects.length];
		int i = 0;
		for(Byte bObj : byteObjects) {
			Byte b = bObj.byteValue();
			bytes[i++] = b;
		}
		return bytes;
	}
}
