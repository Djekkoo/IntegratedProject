package networking;

import java.io.IOException;

/**
 * MultiMonitor is a thread that monitors a MulticastSocket for data received.
 * It invokes a callback with a DataPacket as parameter.
 * 
 * @author Sander Koning <s.koning@student.utwente.nl>
 * @version 0.1
 * @since 2014-04-09
 */

import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.MulticastSocket;

import main.CallbackException;

/**
 * MultiMonitor is a thread that monitors a MulticastSocket for data received.
 * It invokes a callback with a DataPacket as parameter.
 * 
 * @author Sander Koning <s.koning@student.utwente.nl>
 * @version 0.1
 * @since 2014-04-07
 */

public class MultiMonitor extends Thread {

	MulticastSocket mSock = null;
	Networker received = null;
	
	public MultiMonitor(Networker received, MulticastSocket mSock){
		this.received = received;
		this.mSock = mSock;
	}
	
	@Override
	public void run() {
		byte[] buffer = new byte[1024];
		byte[] temp;
		DatagramPacket dpack = new DatagramPacket(buffer, buffer.length);
		SmallPacket dp;

		while (true) {
			try {
				mSock.receive(dpack);
				
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
