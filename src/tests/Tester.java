package tests;

/** 
 * @author      Florian Fikkert <f.a.j.fikkert@student.utwente.nl>
 * @version     0.1
 * @since       2014-04-07
 */
public class Tester {
	public static int errors;

    public static void main(String[] args) {
        System.out.println("Log of " + Tester.class + 
                           ", " + new java.util.Date());
		//run(new GUITest());	
		run(new ClientTest());	
		System.out.println("Total errors: " + errors);
    }

	public static void run(TestCase test) {
		System.out.println("Testklasse: "+test.getClass());
		int error=test.runTest();
        if (error == 0) {
            System.out.println("    OK");
        } else {
			System.out.println("Errors found: "+error);
		}
		errors += error;
	}
}
