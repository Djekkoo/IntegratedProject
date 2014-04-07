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
import main.Communication;

public class Networker implements Runnable {
	public static final int PORT = 1337;
	
	DatagramSocket dSock;
	byte self = 0;
	
	Callback router;
	Communication com;
	
	public Networker(Communication com) throws SocketException{
		dSock = new DatagramSocket(PORT);
		self = dSock.getLocalAddress().getAddress()[3];
		this.com = com;
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

	@Override
	public void run() {
		byte[] buffer = new byte[1024];
		DatagramPacket dpack=new DatagramPacket(buffer, buffer.length);
		DataPacket packet;
		
		while(true){
			try {
				dSock.receive(dpack);
				packet = new DataPacket(dpack.getData());
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (DatagramDataSizeException e) {
				e.printStackTrace();
			}
		}
	}
}
