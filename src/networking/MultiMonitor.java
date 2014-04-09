package networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

import main.Callback;
import main.CallbackException;

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
				System.out.println("sock: " + dpack.getAddress().isAnyLocalAddress());
				System.out.println("mSock inet: " + mSock.getNetworkInterface());
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
