package br.org.funcate.baurudigital.server.photo;

public class PhotoException extends Exception {

	private static final long serialVersionUID = -4008782789308656969L;

	public PhotoException(String message, Throwable cause) {
		super(message, cause);
	}
	public PhotoException(String message) {
		super(message);
	}
}