package main;

public class CryptoException extends Exception {
	
	private static final long serialVersionUID = -2482656411978162877L;

	public CryptoException() {
		super();
	}
	
	public CryptoException(String message) {
		super(message);
	}
	
	public CryptoException(String message, Throwable throwable) {
		super(message, throwable);
	}
}