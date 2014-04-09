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

	public static byte DEVICE = 1;
	public static byte GROUP = 14;
	public static String WLAN = "wlan0";
	public static String BROADCAST = "226.0.0.0/8";
	public Communication communication;

	public static void main(String args[]) throws IOException {
		// startup AD-HOC network
		boolean isWindows = System.getProperty("os.name").startsWith("Windows");
		if (isWindows) {
			
			System.out.println("Please boot this application on Linux");
			//System.exit(0);  even weggehaald omdat ik anders niet kan testen <3 FLO
			
			// linux
		} else {
			try {
				DEVICE = getIP().getAddress()[3];
				System.out.println(DEVICE + " ");
			} catch (SocketException | UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/*
			Runtime.getRuntime().exec(
					"gksudo ./adhoc_linux.sh " + WLAN + " " + GROUP + " "
							+ DEVICE);
			
			System.out.println("set up Ad-hoc connection");
			
			Runtime.getRuntime().exec("gksudo route add -net "+BROADCAST+" dev "+WLAN);
			
			System.out.println("Added route to routing table");
			*/
		}
		
		// register shutdown
		Runtime.getRuntime().addShutdownHook(
		    new Thread() {
		        public void run() {
		        	
		        	System.out.println("Shutting down");
		        	boolean isWindows = System.getProperty("os.name").startsWith("Windows");
		        	if (isWindows) {
			        	
		        		
		        	// linux
		        	} else {
		        		/*try {
							Runtime.getRuntime().exec("sudo route del -net "+BROADCAST+" dev "+WLAN);
							System.out.println("Deleted route from routing table");
						} catch (IOException e) {
							System.out.println("Could not delete route: " + e.getLocalizedMessage());
						}*/
		        	}
		        }
		    }
		);
		
		// Start project
		new IntegrationProject();

	}

	public IntegrationProject() {
		
		new Communication();

	}
	
	private static InetAddress getIP() throws SocketException, UnknownHostException {
		Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
		NetworkInterface iface;
		Enumeration<InetAddress> addresses;
		InetAddress ip;
		while(ifs.hasMoreElements() && (iface = ifs.nextElement()) != null)
		{
			if(iface.getDisplayName().equals("wlan0")) {
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
