package br.org.funcate.baurudigital.server.form;

public class FormException extends Exception {

	private static final long serialVersionUID = -4008782789308656969L;

	public FormException(String message, Throwable cause) {
		super(message, cause);
	}
	public FormException(String message) {
		super(message);
	}
}
