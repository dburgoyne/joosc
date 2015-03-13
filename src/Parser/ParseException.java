package Parser;

public class ParseException extends Exception {

	private static final long serialVersionUID = -8329758053428182268L;

	public ParseException() { }

	public ParseException(String arg0) {
		super(arg0);
	}

	public ParseException(Throwable cause) {
		super(cause);
	}

	public ParseException(String message, Throwable cause) {
		super(message, cause);
	}
}
