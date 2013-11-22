package br.org.funcate.baurudigital.server.task;

public class TaskException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4008782789308656969L;

	public TaskException(String message, Throwable cause) {
		super(message, cause);
	}
	public TaskException(String message) {
		super(message);
	}
}