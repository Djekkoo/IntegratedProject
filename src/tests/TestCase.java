package tests;

/** 
 * @author      Florian Fikkert <f.a.j.fikkert@student.utwente.nl>
 * @version     0.1
 * @since       2014-04-07
 */
public abstract class TestCase {
	
	private String description;
	private boolean isPrinted;
	protected int errors;
	
	
	abstract protected int runTest();
	abstract protected void setUp();
	
	/**
     * Fixeert de beschrijving van de huidige testmethode.
     * @param text de te printen beschrijving
     */
	protected void startTest(String text) {
        description = text;
        // de beschrijving is nog niet geprint
        isPrinted = false;
    }
	
	 /**
     * Test of de werkelijke waarde van een geteste expressie 
     * overeenkomt met de verwacht (correcte) waarde.
     * Deze implementatie print beide waarden, plus een aanduiding van
     * wat er getest is, op de standaarduitvoer: het programma voert
     * geen vergelijking uit.
     */
	 protected void assertEquals(String tekst, Object verwacht, Object werkelijk) {
        boolean gelijk;
        if (verwacht == null) {
            gelijk = werkelijk == null;
        } else {
            gelijk = werkelijk != null && verwacht.equals(werkelijk);
        }
        if (! gelijk) {
            if (! isPrinted) {
                System.out.println("    Test: "+description);
                isPrinted = true;
            }
            System.out.println("        " + tekst);
            System.out.println("            Expected:  " + verwacht);
            System.out.println("            Reality: " + werkelijk);
            errors++;
        }
    }
}