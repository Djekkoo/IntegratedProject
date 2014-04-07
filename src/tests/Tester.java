package tests;

public class Tester {
	public static int fouten;

    public static void main(String[] args) {
        System.out.println("Log van " + Tester.class + 
                           ", " + new java.util.Date());
		run(new GUITest());		
		System.out.println("Totaal aantal fouten gevonden: " + fouten);
    }

	public static void run(TestCase test) {
		System.out.println("Testklasse: "+test.getClass());
		int fout=test.runTest();
        if (fout == 0) {
            System.out.println("    OK");
        } else {
			System.out.println("Fouten gevonden: "+fout);
		}
		fouten += fout;
	}
}
