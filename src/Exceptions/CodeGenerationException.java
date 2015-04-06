package Exceptions;

import AbstractSyntax.Expression;
import AbstractSyntax.TypeDecl;

public class CodeGenerationException extends Exception {
	
	private static final long serialVersionUID = -1111335513845684886L;

	public CodeGenerationException(String msg) {
		super(msg);
	}

	public static class InvalidLValue extends CodeGenerationException {

		private static final long serialVersionUID = 3123771292762975134L;

		public InvalidLValue(Expression expr) {
			super(String.format("Expression cannot be assigned to.\n"
					+ " at %s\n",
					expr.getPositionalString()));
		}
	}
	
	public static class NoStaticIntTest extends CodeGenerationException {

		private static final long serialVersionUID = 8123451163202210658L;

		public NoStaticIntTest(TypeDecl t) {
			super(String.format("Type %s must define a method \"public static int test()\"\n"
					+ " at %s\n",
					t.getName().toString(),
					t.getPositionalString()));
		}
	}
}
