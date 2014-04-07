package main;

public class CallbackException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 997299158292356685L;

	@SuppressWarnings("rawtypes")
	public CallbackException(Class c, String e) {
		super("Error type: " + c.toString() + ", message: " + e);
	}
}
