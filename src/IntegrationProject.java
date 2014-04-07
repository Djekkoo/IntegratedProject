import java.awt.EventQueue;
import java.io.IOException;

public class IntegrationProject {

	public static int DEVICE = 1;
	public static int GROUP = 13;
	public static String WLAN = "Wlan1Groepje";
	public Communication communication;

	public static void main(String args[]) throws IOException {

		boolean isWindows = System.getProperty("os.name").startsWith("Windows");

		if (isWindows) {

			// linux
		} else {
			Runtime.getRuntime().exec(
					"sudo ./adhoc_linux.sh " + WLAN + " " + GROUP + " "
							+ DEVICE);
		}

		new IntegrationProject();

	}

	public IntegrationProject() {
		communication = new Communication();
		//Maak een Client aan met zijn eigen communication object.
		new Client(communication);
	}

}
