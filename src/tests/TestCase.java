package tests;

public abstract class TestCase {
	
	private String beschrijving;
	private boolean isGeprint;
	protected int fouten;
	
	
	abstract protected int runTest();
	abstract protected void setUp();
	
	/**
     * Fixeert de beschrijving van de huidige testmethode.
     * @param tekst de te printen beschrijving
     */
	protected void beginTest(String tekst) {
        beschrijving = tekst;
        // de beschrijving is nog niet geprint
        isGeprint = false;
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
        // test op gelijkheid van verwacht en werkelijk,
        // rekening houdend met de waarde null
        if (verwacht == null) {
            gelijk = werkelijk == null;
        } else {
            gelijk = werkelijk != null && verwacht.equals(werkelijk);
        }
        if (! gelijk) {
            // print eventueel de beschijving van de testmethode
            if (! isGeprint) {
                System.out.println("    Test: "+beschrijving);
                // nu is de beschrijving geprint
                isGeprint = true;
            }
            System.out.println("        " + tekst);
            System.out.println("            Verwacht:  " + verwacht);
            System.out.println("            Werkelijk: " + werkelijk);
            fouten++;
        }
    }
}