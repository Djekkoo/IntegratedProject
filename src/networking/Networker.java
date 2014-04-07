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

public class Networker {
	public static final int PORT = 1337;
	
	DatagramSocket dSock;
	byte self = 0;
	
	Callback router;
	
	public Networker() throws SocketException{
		dSock = new DatagramSocket(PORT);
		self = dSock.getLocalAddress().getAddress()[3];
		
	}
	
	public void broadcast(byte[] data){
		
	}
	
	public void setRouter(Callback router){
		this.router = router;
	}

	public void send(byte destination, byte[] data) {
		byte hops = 0x0F;
		
		byte sequencenr = (byte) ((new Random()).nextInt() >>> 24);
		
		byte connection = 0;
		try {
			connection = (byte) router.invoke(new Object());
		} catch (CallbackException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}// getRoute()
		
		if(data.length <= 1016){
			DataPacket dp = new DataPacket(self, destination, hops, sequencenr, data, false, false, false);
			try {
				dSock.send(new DatagramPacket(dp.getRaw(), dp.getRaw().length, getFullAddress(connection), PORT));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			// chop into packets
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
}
