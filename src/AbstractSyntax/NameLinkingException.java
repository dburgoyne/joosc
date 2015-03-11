package AbstractSyntax;

public abstract class NameLinkingException extends Exception {

	private static final long serialVersionUID = -1108416686603403175L;

	public NameLinkingException() { }

	public NameLinkingException(String message) {
		super(message);
	}

	public NameLinkingException(Throwable cause) {
		super(cause);
	}

	public NameLinkingException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public static class NonexistentMethod extends NameLinkingException {

		private static final long serialVersionUID = -1208140640801030509L;
		
		public NonexistentMethod(Identifier methodName) {
			super("Nonexistent method " + methodName + " called.");
		}
	}
	
}
