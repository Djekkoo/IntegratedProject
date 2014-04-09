package networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import main.Callback;
import main.CallbackException;

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
		DatagramPacket dpack = new DatagramPacket(buffer, buffer.length);

		while (true) {
			try {
				dSock.receive(dpack);
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
