package networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import main.Callback;
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
	Callback received = null;
	
	public UniMonitor(Callback received, DatagramSocket mSock){
		this.received = received;
		this.dSock = mSock;
	}
	
	@Override
	public void run() {
		byte[] buffer = new byte[1024];
		byte[] temp;
		DatagramPacket dpack = new DatagramPacket(buffer, buffer.length);

		while (true) {
			try {
				dSock.receive(dpack);
				
				temp = new byte[dpack.getLength()];
				
				System.arraycopy(dpack.getData(), dpack.getOffset(), temp, 0, dpack.getLength());
				
				received.invoke(new SmallPacket(temp), dpack.getAddress());
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
