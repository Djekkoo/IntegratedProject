package routing;

import java.nio.ByteBuffer;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;
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
	
	/**
	 * A map of sets of neighbours paired to the network IDs
	 */
	private TreeMap<Byte,TreeSet<Byte>> networkTreeMap = new TreeMap<Byte,TreeSet<Byte>>();
	/**
	 * The timestamp of the last received packet
	 */
	private long lastReceivedPacketTimestamp = 0;
	/**
	 * The timestamp of the last sent packet
	 */
	private long lastSentPacketTimestamp = 0;
	/**
	 * A callback, invoking this will run networking.Networker.send()
	 */
	private Callback sendMethod;
	/**
	 * A callback, invoking this will run main.Communication.updateNetwork()
	 */
	private Callback updateMethod;
	/**
	 * The device ID ie., the last byte of the IP on wlan0
	 */
	private byte deviceID;
	/**
	 * Contains all the nodes as Vertex objects.
	 */
	private Vertex[] vertexArray;
	/**
	 * A cache of the next client a packet should go to for a given route.
	 */
	private HashMap<Byte,Byte> nextHops = new HashMap<Byte,Byte>();
	/**
	 * A cache of the route lengths, from this device to another.
	 */
	private HashMap<Byte,Byte> routeLengths = new HashMap<Byte,Byte>();
	/**
	 * Tells the class if auto updating is enabled.
	 */
	private boolean autoUpdate=true;
	/**
	 * Locks some methods while busy.
	 */
	private ReentrantLock lock = new ReentrantLock();
	
	//PUBLIC
	
	public LinkStateRouting(Callback send, Callback newUser) {
		this.sendMethod = send;
		this.updateMethod = newUser;
		this.deviceID = IntegrationProject.DEVICE;
		
		networkTreeMap.put(deviceID, new TreeSet<Byte>());
		
		if(autoUpdate) {
			update();	
		}
	}
	
	public LinkStateRouting(Callback send, Callback newUser, boolean autoUpdate) {
		this.sendMethod = send;
		this.updateMethod = newUser;
		this.deviceID = IntegrationProject.DEVICE;
		this.autoUpdate = autoUpdate;
		
		networkTreeMap.put(deviceID, new TreeSet<Byte>());
		
		if(autoUpdate) {
			update();	
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void packetReceived(DataPacket p) {
		// TODO Handle packet receives
		boolean updated = parsePacket(p.getData());
		if(updated) {
			sendToNeighbours(buildPacket());
			update();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	//SimpleEntry: NextHop, Distance
	public SimpleEntry<Byte, Byte> getRoute(Byte destination)
			throws RouteNotFoundException {
		lock.lock();
		// TODO Respond whenever a route is requested.
		if(!networkTreeMap.containsKey(destination))
		{
			lock.unlock();
			throw new RouteNotFoundException("Destination unknown.");
		} else if(networkTreeMap.get(destination).isEmpty()
				|| networkTreeMap.get(deviceID).isEmpty()) {
			lock.unlock();
			throw new RouteNotFoundException("Destination unreachable; no route to host.");
		}
		
		Byte nextHop = nextHops.get(destination);
		Byte routeLen = routeLengths.get(destination);
		if(nextHop == -1 || routeLen == -1) {
			if(autoUpdate) {
				update();
			}
			lock.unlock();
			throw new RouteNotFoundException("Destination unreachable; no route to host.");
		}
		
		lock.unlock();
		return new SimpleEntry<Byte,Byte>(nextHop,routeLen);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void networkMessage(Byte node, NetworkMessage type) {
		// TODO Handle messages
		switch(type) {
		case NEWKEEPALIVE:
			networkTreeMap.put(node,new TreeSet<Byte>());
			this.addPath(deviceID, node);
			send(node,buildPacket());
			break;
		case DROPPED:
			this.removeNode(node);
			break;
		case NOKEEPALIVE:
			this.removePath(this.deviceID, node);
			break;
		default:
			break;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Boolean isReachable(Byte node) {
		return (findPath(deviceID,node) == null);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Byte getLongestRoute() {
		Byte max = 0;
		for(Entry<Byte,Byte> e : routeLengths.entrySet()) {
			Byte nextHop = e.getValue();
			if(nextHop > max) {
				max = nextHop;
			}
		}
		return max;
	}
	
	/**
	 * Prints the nodes and prints what nodes they are connected to.
	 * 
	 * @since	2014-04-08
	 */
	public void showNetwork() {
		for(Entry<Byte,TreeSet<Byte>> e : networkTreeMap.entrySet()) {
			System.out.println(e.getKey() + " to: ");
			for(Byte b : e.getValue()) {
				System.out.println("\t" + b);
			}
		}
	}
	
	/**
	 * Creates a path between two nodes in the network map.
	 * 
	 * @param	A	The first node.
	 * @param	B	The second node.
	 * @since	2014-04-09
	 */
	public void addPath(Byte A, Byte B) {
		networkTreeMap.get((byte)A).add((byte)B);
		networkTreeMap.get((byte)B).add((byte)A);
		if(autoUpdate) {
			update();
		}
	}
	
	/**
	 * Removes a path between two nodes in the network map.
	 * 
	 * @param	A	The first node.
	 * @param	B	The second node.
	 * @since	2014-04-09
	 */
	public void removePath(Byte A, Byte B) {
		networkTreeMap.get((byte)A).remove((byte)B);
		networkTreeMap.get((byte)B).remove((byte)A);
		if(autoUpdate) {
			update();
		}
	}
	
	/**
	 * Adds a node to the network map.
	 * 
	 * @param	N	The node id to add to the network.
	 * @since	2014-04-10
	 */
	public void addNode(Byte N) {
		networkTreeMap.put(N, new TreeSet<Byte>());
		if(autoUpdate) {
			update();
		}
	}
	
	/**
	 * Removes a node to the network map and removes its links.
	 * 
	 * @param	N	The node id to remove from the network and removes 
	 * @since	2014-04-10
	 */
	public void removeNode(Byte N) {
		for(Object nb : networkTreeMap.get(N).toArray()) {
			removePath((Byte)nb, this.deviceID);
		}
		networkTreeMap.put(N, new TreeSet<Byte>());
		if(autoUpdate) {
			update();
		}
	}
	
	
	/**
	 * Builds the cached list of route lengths and next hops.
	 * 
	 * @since	2014-04-09
	 */
	public void update() {
		lock.lock();
		HashMap<Byte,LinkedList<Vertex>> paths = findAllPaths();
		nextHops.clear();
		routeLengths.clear();
		for(Entry<Byte,LinkedList<Vertex>> path : paths.entrySet()) {
			Byte id = path.getKey();
			if(path.getValue() != null) {
				Byte nextHop = Byte.parseByte(path.getValue().get(1).getId());
				nextHops.put(id,nextHop);
				routeLengths.put(id,(byte)(path.getValue().size()-2));
			} else {
				nextHops.put(id,(byte)(-1));
				routeLengths.put(id,(byte)(-1));
			}
		}
		lock.unlock();
	}

	
	// PRIVATE
	
	/**
	 * Finds a path between to nodes.
	 * 
	 * @param	src The source node
	 * @param	dst The destination node
	 * @return	A linked list of nodes (A path)
	 * @since	2014-04-09
	 */
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
	
	/**
	 * Finds all the paths from the current node to all other (known) nodes.
	 * 
	 * @return	A K,V map of paths set to the device IDs.
	 * @since	2014-04-09
	 */
	private HashMap<Byte,LinkedList<Vertex>> findAllPaths() {
		DijkstraAlgorithm pf;
		HashMap<Byte, LinkedList<Vertex>> paths = new HashMap<Byte,LinkedList<Vertex>>();
		pf = getPathFinder();
		pf.execute(vertexArray[deviceID]);
		for(Vertex v : vertexArray) {
			if(v != null) {
				Byte vID = Byte.parseByte(v.getId());
				paths.put(vID,pf.getPath(v));
			}
		}
		return paths;
	}
	
	/**
	 * Used to send a notification of a non-neighbour node that joined or dropped (it like it's hot).
	 * 
	 * @param 	node The node the notification is about.
	 * @param 	joined True if it's a new node. False if the node dropped.
	 */
	private void userNotification(Byte node, boolean joined) {
		if(this.updateMethod != null) {
			NetworkMessage m = (joined) ? NetworkMessage.JOINED : NetworkMessage.DROPPED;
			try {
				updateMethod.invoke(node, m);
			} catch (CallbackException e) {
				e.getException().printStackTrace();
			}
		}
	}
	
	/**
	 * Takes the network map and puts it in graph form so the pathfinder
	 * can work with it. Then builds the pathfinder and returns it.
	 * 
	 * @return 	The pathfinder object
	 * @since	2014-04-09
	 */
	private DijkstraAlgorithm getPathFinder() {
		List<Vertex> vertices = new ArrayList<Vertex>();
		List<Edge> edges = new ArrayList<Edge>();
		
		this.vertexArray = new Vertex[16];
		
		for(Entry<Byte,TreeSet<Byte>> neighbour : networkTreeMap.entrySet()) {
			String id = neighbour.getKey().toString();
			Vertex v = new Vertex(id,id);
			vertices.add(v);
			vertexArray[neighbour.getKey()] = v;
		}
		for(Entry<Byte,TreeSet<Byte>> neighbour : networkTreeMap.entrySet()) {
			for(Byte b : neighbour.getValue()) {
				String name = b.toString() + neighbour.getKey().toString();
				edges.add(new Edge(name,vertexArray[neighbour.getKey()],vertexArray[b],1));
			}
		}
		Graph network = new Graph(vertices, edges);
		DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(network);
		return dijkstra;
	}
	
	/**
	 * Sends the data to all the neighbours of the current device.
	 * 
	 * @param	data	The bytes to send to the neighbours
	 * @since	2014-04-09
	 */
	private void sendToNeighbours(Byte[] data) {
		TreeSet<Byte> neighbours = networkTreeMap.get(deviceID);
		for(Byte nb : neighbours) {
			send(nb,data);
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
		if(timestamp > lastReceivedPacketTimestamp && timestamp > lastSentPacketTimestamp) {
			lastReceivedPacketTimestamp = timestamp;
			//Get how many hosts there are in the data
			int numHosts = p[8];
			int offset = 9;
			for(int i = 0; i < numHosts; i++) {
				try {
					//Get the host of the entry
					byte host = p[0+offset];
					//Get how many neighbours are in the entry
					int numNeighbours = p[1+offset];
					byte[] neighbours = new byte[numNeighbours];
					System.arraycopy(p, 2+offset, neighbours, 0, numNeighbours);
					offset += numNeighbours+2;
					
					TreeSet<Byte> oldNeighbours = new TreeSet<Byte>();
					for(Object nb : networkTreeMap.get(host).toArray()) {
						oldNeighbours.add((Byte)nb);
					}
					
					for(byte nb : neighbours) {
						//Do we have a host that we have no record of?
						if(networkTreeMap.containsKey(host)) {
							if(networkTreeMap.get(host).contains(nb)) {
								//Do nothing
							} else {
								this.addPath(host, nb);
								updated = true;
							}
							oldNeighbours.remove(nb);
						} else {
							this.addNode(host);
							this.addPath(host, nb);
							sendMethod.invoke(host,NetworkMessage.JOINED);
							oldNeighbours.remove(nb);
							updated = true;
						}
					}
					for(Byte nb : oldNeighbours) {
						this.removeNode(nb);
						sendMethod.invoke(nb,NetworkMessage.DROPPED);
						updated = true;
					}
				} catch(CallbackException e) {
					e.getException().printStackTrace();
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
		lastSentPacketTimestamp = timestamp;
		byte[] timestampBytes = ByteBuffer.allocate(8).putLong(timestamp).array();
		for(byte b : timestampBytes) {
			p.add((Byte)b);
		}
		
		// Add the host count.
		p.add(Byte.valueOf((byte)(networkTreeMap.size()&0xFF)));
		for (Entry<Byte,TreeSet<Byte>> e : networkTreeMap.entrySet()) {
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
	 * Prepares the Byte array for sending, then invokes the callback to send.
	 * 
	 * @param 	node The node to send to.
	 * @param 	data The data to send to the node.
	 * @since	2014-04-09
	 */
	private void send(Byte node, Byte[] data) {
		try {
			if(sendMethod != null) {
				sendMethod.invoke(node, toByteArray(data));
			}
		} catch (CallbackException e) {
			// TODO Auto-generated catch block
			e.getException().printStackTrace();
		}
	}
	
	/**
	 * Takes an ArrayList of Bytes and turns it into a Byte[]
	 * 
	 * @param	bytes The ArrayList of Bytes.
	 * @return	The bytes in the ArrayList, contained in a Byte[]
	 * @since	2014-04-09
	 */
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
	
	/**
	 * Takes a primitive array of bytes and turns it into a Byte[]
	 * 
	 * @param	bytes The primitive byte array.
	 * @return	The byte array as a Byte[]
	 * @since	2014-04-09
	 */
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
	
	/**
	 * Takes an object array of Bytes and turns it into a primitive
	 * array of bytes.
	 * 
	 * @param	byteObjects The Byte object array.
	 * @return	The primitive byte array
	 * @since	2014-04-09
	 */
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
