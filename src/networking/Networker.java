package networking;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Networker {
	DatagramSocket dSock;
	byte self = 0;
	
	public Networker() throws SocketException{
		dSock = new DatagramSocket(1337);
	}
	
	public void broadcast(byte[] data){
		
	}

	public void send(byte destination, byte[] data) {
		// getRoute()
		
		if(data.length <= 1016){
			// make packet
		}else{
			// chop into packets
		}
	}
}
