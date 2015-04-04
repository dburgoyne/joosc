package Exceptions;

import AbstractSyntax.Expression;
import AbstractSyntax.Identifier;
import Types.Type;

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
			super("Nonexistent method " + methodName + " called.\n at "
				+ methodName.getPositionalString());
		}
	}

	public static class BadStatic extends NameLinkingException {
		private static final long serialVersionUID = 3944228727503523513L;
		public BadStatic(Expression e) {
			super("Expression `" + e + "' occurs in a static context.\n at " + 
					e.getPositionalString());
		}
	}
	
	public static class BadNonStatic extends NameLinkingException {
		private static final long serialVersionUID = 3944228727503523513L;
		public BadNonStatic(Expression e) {
			super("Expression `" + e + "' occurs in a non-static context.\n at " + 
					e.getPositionalString());
		}
	}
	
	public static class TypeAsExpr extends NameLinkingException {
		private static final long serialVersionUID = 3944228727503523513L;
		public TypeAsExpr(Expression e, Type t) {
			super("Type `" + t + "' occurs in an expression context.\n at " + 
					e.getPositionalString());
		}
	}
	
	public static class NotFound extends NameLinkingException {
		private static final long serialVersionUID = 3944228727503523513L;
		public NotFound(Expression e) {
			super("Name `" + e + "' cannot be resolved.\n at " + 
					e.getPositionalString());
		}
	}
	
	public static class AmbiguousName extends NameLinkingException {
		private static final long serialVersionUID = 3944228727503523514L;
		public AmbiguousName(Expression e) {
			super("Name `" + e + "' is ambiguous.\n at " + 
					e.getPositionalString());
		}
	}
	
	public static class ForwardReference extends NameLinkingException {
		private static final long serialVersionUID = 3944228727503523554L;
		public ForwardReference(Expression e) {
			super("Forward reference to " + e + ".\n at " + 
					e.getPositionalString());
		}
	}
	
}
