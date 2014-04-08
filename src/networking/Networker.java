package networking;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Random;

import main.Callback;
import main.CallbackException;

/** 
 * @author      Sander Koning <s.koning@student.utwente.nl>
 * @version     0.1
 * @since       2014-04-07
 */
public class Networker implements Runnable {
	public static final int PORT = 1337;
	
	DatagramSocket dSock;
	byte self = 0;
	
	Callback routerGetRoute;
	Callback routerPacketReceived;
	
	public Networker(Callback routerPacketReceived) throws SocketException{
		dSock = new DatagramSocket(PORT);
		self = dSock.getLocalAddress().getAddress()[3];
		this.routerPacketReceived = routerPacketReceived;
	}
	
	public void broadcast(byte[] data) throws IOException{
		dSock.send(new DatagramPacket(data, data.length, InetAddress.getByAddress(new byte[]{(byte) 226,0,0,0}), PORT));
	}
	
	public void setRouter(Callback router){
		this.routerGetRoute = router;
	}

	public void send(byte destination, byte[] data) throws IOException {
		LinkedList<DataPacket> packets = processData(destination, data);
		
		byte connection = 0;
		
		try {
			connection = (byte) routerGetRoute.invoke(new Byte(destination));
		} catch (CallbackException e1) {
			e1.printStackTrace();
		}
		
		for(DataPacket p : packets){
			dSock.send(new DatagramPacket(p.getRaw(), p.getRaw().length, getFullAddress(connection), PORT));
		}
	}
	
	private byte[] processPackets(LinkedList<DataPacket> packets){
		
		int maxChunkSize = 1024 - DataPacket.HEADER_LENGTH;
		int length = (packets.size() - 1) * maxChunkSize + packets.getLast().getData().length;
		
		byte[] result = new byte[length];
		byte[] buffer = new byte[0];
		
		int i = 0;
		while(!packets.isEmpty()){
			buffer = packets.pop().getData();
			System.arraycopy(buffer, 0, result, i*maxChunkSize, buffer.length);
			i++;
		}
		
		return result;
	}
	
	private LinkedList<DataPacket> processData(byte destination, byte[] data){
		LinkedList<DataPacket> result = new LinkedList<DataPacket>();
		
		DataPacket dp;
		
		byte hops = 0x0F;		
		byte sequencenr = (byte) ((new Random()).nextInt() >>> 24);
		
		int maxChunkSize = 1024 - DataPacket.HEADER_LENGTH;
		byte[] chunk = new byte[maxChunkSize];
		
		for (int i = 0; i < Math.ceil(data.length / maxChunkSize); i++){
			if(data.length >= (i+1) * maxChunkSize){
				System.arraycopy(data, i * maxChunkSize, chunk, 0, maxChunkSize);
			} else {
				chunk = new byte[data.length - i * maxChunkSize];
				System.arraycopy(data, i * maxChunkSize, chunk, 0, data.length - i * maxChunkSize);
			}
			
			try {
				dp = new DataPacket(self, destination, hops, sequencenr, chunk, false, false, false);
				result.add(dp);
			} catch (DatagramDataSizeException e) {
				e.printStackTrace();
			}
		}
		
		return result;
		
	}
	
	private InetAddress getFullAddress(byte postfix){
		
		byte[] myAddress = dSock.getLocalAddress().getAddress();
		myAddress[3] = postfix;
		try {
			return InetAddress.getByAddress(myAddress);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} 
		return null;
	}

	@Override
	public void run() {
		byte[] buffer = new byte[1024];
		DatagramPacket dpack = new DatagramPacket(buffer, buffer.length);
		DataPacket packet;
		
		while(true){
			try {
				dSock.receive(dpack);
				packet = new DataPacket(dpack.getData());
				if(packet.isRouting())
					routerPacketReceived.invoke(packet);
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (DatagramDataSizeException e) {
				e.printStackTrace();
			} catch (CallbackException e) {
				e.printStackTrace();
			}
		}
	}
}
