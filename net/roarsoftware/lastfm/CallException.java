package net.roarsoftware.lastfm;

/**
 * @author Janni Kovacs
 */
@SuppressWarnings("serial")
public class CallException extends RuntimeException {

	public CallException() {
	}

	public CallException(Throwable cause) {
		super(cause);
	}

	public CallException(String message) {
		super(message);
	}

	public CallException(String message, Throwable cause) {
		super(message, cause);
	}
}
