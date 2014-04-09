package main;

import java.io.IOException;

/** 
 * @author      Jacco Brandt <j.h.brandt@student.utwente.nl>
 * @version     0.1
 * @since       2014-04-07
 */
public class IntegrationProject {

	public static byte DEVICE = 1;
	public static byte GROUP = 14;
	public static String WLAN = "wlan0";
	public static String BROADCAST = "266.0.0.0/8";
	public Communication communication;

	public static void main(String args[]) throws IOException {

		// startup AD-HOC network
		boolean isWindows = System.getProperty("os.name").startsWith("Windows");
		if (isWindows) {
			
			System.out.println("Please boot this application on Linux");
			//System.exit(0);  even weggehaald omdat ik anders niet kan testen <3 FLO
			
			// linux
		} else {
			
			Runtime.getRuntime().exec(
					"gksudo ./adhoc_linux.sh " + WLAN + " " + GROUP + " "
							+ DEVICE);
			
			System.out.println("set up Ad-hoc connection");
			
			Runtime.getRuntime().exec("gksudo route add -net "+BROADCAST+" dev "+WLAN);
			System.out.println("Added route to routing table");
			
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
							Runtime.getRuntime().exec("gksudo route del -net "+BROADCAST+" dev "+WLAN);
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
