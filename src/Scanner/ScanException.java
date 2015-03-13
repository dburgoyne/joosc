package Scanner;

public class ScanException extends Exception {

	private static final long serialVersionUID = 148932378711751902L;

	public ScanException() { }

	public ScanException(String arg0) {
		super(arg0);
	}

	public ScanException(Throwable cause) {
		super(cause);
	}

	public ScanException(String message, Throwable cause) {
		super(message, cause);
	}
}
