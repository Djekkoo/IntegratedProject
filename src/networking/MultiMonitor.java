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
import java.net.MulticastSocket;

import main.Callback;
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
	Callback received = null;
	
	public MultiMonitor(Callback received, MulticastSocket mSock){
		this.received = received;
		this.mSock = mSock;
	}
	
	@Override
	public void run() {
		byte[] buffer = new byte[1024];
		DatagramPacket dpack = new DatagramPacket(buffer, buffer.length);

		while (true) {
			try {
				mSock.receive(dpack);
				if(!(dpack.getSocketAddress().equals(mSock.getInetAddress())))
					received.invoke(new DataPacket(dpack.getData()));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (DatagramDataSizeException e) {
				e.printStackTrace();
			} catch (CallbackException e) {
				e.printStackTrace();
			} finally {
				buffer = new byte[1024];
				dpack = new DatagramPacket(buffer, buffer.length);
			}
		}
	}
}
