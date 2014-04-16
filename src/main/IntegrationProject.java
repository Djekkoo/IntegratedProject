package main;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/** 
 * @author      Jacco Brandt <j.h.brandt@student.utwente.nl>
 * @version     0.1
 * @since       2014-04-07
 */
public class IntegrationProject {

	private static final byte MAXNODES = 16;
	
	public static byte DEVICE = 1;
	public static byte GROUP = 14;
	public static String WLAN = "wlan0";
	public static String BROADCAST = "226.0.0.0";
	
	private static Communication Communication;

	public static void main(String args[]) throws IOException {
		// startup AD-HOC network
		boolean isWindows = System.getProperty("os.name").startsWith("Windows");
		if (isWindows) {
			
			System.out.println("Please boot this application on Linux");
			System.exit(0);//  even weggehaald omdat ik anders niet kan testen <3 FLO
			
			// linux
		} else {
			try {
				DEVICE = getIP("wlan0").getAddress()[3];
				System.out.println(DEVICE + " ");
			} catch (SocketException | UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(DEVICE < MAXNODES && DEVICE > 0)
		{
			// register shutdown
			Runtime.getRuntime().addShutdownHook(
			    new Thread() {
			        public void run() {
			        	
			        	System.out.println("Shutting down");
			        	IntegrationProject.Communication.shutDown();
			        	
			        }
			    }
			);
			
			// Start project
			new IntegrationProject();
		} else {
			System.err.println("It appears you are not in the Ad-hoc network. Shutting down.");
		}
	}

	public IntegrationProject() {
		
		IntegrationProject.Communication = new Communication();

	}
	
	/**
	 * Gives the IP currently in use on an interface.
	 * 
	 * @param	ifacename The short name of the interface to request the IP of.
	 * @return	The IP currently in use on the interface.
	 * @throws 	SocketException
	 * @throws 	UnknownHostException
	 */
	private static InetAddress getIP(String ifacename) throws SocketException, UnknownHostException {
		Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
		NetworkInterface iface;
		Enumeration<InetAddress> addresses;
		InetAddress ip;
		while(ifs.hasMoreElements() && (iface = ifs.nextElement()) != null)
		{
			if(iface.getDisplayName().equals(ifacename)) {
				addresses = iface.getInetAddresses();
				
				while(addresses.hasMoreElements() && (ip = addresses.nextElement()) != null) {
					if(ip instanceof Inet4Address) {
						return ip;
					}
				}
			}
		}
		return InetAddress.getLocalHost();
	}

}
