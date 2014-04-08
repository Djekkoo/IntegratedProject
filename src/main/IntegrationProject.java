package main;

import java.io.IOException;

/** 
 * @author      Jacco Brandt <j.h.brandt@student.utwente.nl>
 * @version     0.1
 * @since       2014-04-07
 */
public class IntegrationProject {

	public static byte DEVICE = 1;
	public static byte GROUP = 13;
	public static String WLAN = "Wlan1Groepje";
	public static String BROADCAST = "266.0.0.0/8";
	public Communication communication;

	public static void main(String args[]) throws IOException {

		// startup AD-HOC network
		boolean isWindows = System.getProperty("os.name").startsWith("Windows");
		if (isWindows) {

			// linux
		} else {
			
			Runtime.getRuntime().exec("sudo route add -net "+BROADCAST+" dev "+WLAN);
			System.out.println("Added route to routing table");
			
			Runtime.getRuntime().exec(
					"sudo ./adhoc_linux.sh " + WLAN + " " + GROUP + " "
							+ DEVICE);
			
			System.out.println("set up Ad-hoc connection");
			
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
		        		try {
							Runtime.getRuntime().exec("sudo route del -net "+BROADCAST+" dev "+WLAN);
							System.out.println("Deleted route from routing table");
						} catch (IOException e) {
							System.out.println("Could not delete route: " + e.getLocalizedMessage());
						}
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

}
