package main;

/** 
 * @author      Jacco Brandt <j.h.brandt@student.utwente.nl>
 * @version     0.1
 * @since       2014-04-07
 */
public class CallbackException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 997299158292356685L;
	private Exception exception;

	@SuppressWarnings("rawtypes")
	public CallbackException(Class c, String e, Exception exception) {
		super("Error type: " + c.toString() + ", message: " + e);
		this.exception = exception;
	}
	
	public Exception getException() {
		return this.exception;
	}
	
}
