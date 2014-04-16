package networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;

import main.CallbackException;

/**
 * UniMonitor is a thread that monitors a DatagramSocket for data received.
 * It invokes a callback with a DataPacket as parameter.
 * 
 * @author Sander Koning <s.koning@student.utwente.nl>
 * @version 0.1
 * @since 2014-04-07
 */

public class UniMonitor extends Thread {

	DatagramSocket dSock = null;
	Networker received = null;
	
	public UniMonitor(Networker received, DatagramSocket mSock){
		this.received = received;
		this.dSock = mSock;
	}
	
	@Override
	public void run() {
		Thread.currentThread().setName("Unicast Monitor");
		byte[] buffer = new byte[1024];
		byte[] temp;
		DatagramPacket dpack = new DatagramPacket(buffer, buffer.length);
		SmallPacket dp;
		
		while (true) {
			try {
				dSock.receive(dpack);
				
				temp = new byte[dpack.getLength()];
				
				System.arraycopy(dpack.getData(), dpack.getOffset(), temp, 0, dpack.getLength());
				
				dp = new SmallPacket(temp);
				
				received.receive(dp, (Inet4Address) dpack.getAddress());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (DatagramDataSizeException e) {
				e.printStackTrace();
			} finally {
				buffer = new byte[1024];
				dpack = new DatagramPacket(buffer, buffer.length);
			}
		}
	}
}
