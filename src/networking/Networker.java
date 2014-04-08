package networking;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
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
	
	public void broadcast(byte[] data){
		this.send((byte)0xFF,data);
	}
	
	public void setRouter(Callback router){
		this.routerGetRoute = router;
	}

	public void send(byte destination, byte[] data) {
		byte hops = 0x0F;
		
		byte sequencenr = (byte) ((new Random()).nextInt() >>> 24);
		
		byte connection = 0;
		try {
			// TODO give proper arguments
			connection = (byte) routerGetRoute.invoke(new Byte(destination));
		} catch (CallbackException e1) {
			e1.printStackTrace();
		}
		
		if(data.length <= 1016){
			try {
				DataPacket dp = new DataPacket(self, destination, hops, sequencenr, data, false, false, false);
				dSock.send(new DatagramPacket(dp.getRaw(), dp.getRaw().length, getFullAddress(connection), PORT));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (DatagramDataSizeException e) {
				e.printStackTrace();
			}
		}else{
			// TODO chop into packets
		}
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
		DatagramPacket dpack=new DatagramPacket(buffer, buffer.length);
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
