package tests;

public class Tester {
	public static int errors;

    public static void main(String[] args) {
        System.out.println("Log of " + Tester.class + 
                           ", " + new java.util.Date());
		run(new GUITest());		
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