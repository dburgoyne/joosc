package AbstractSyntax;

public abstract class TypeLinkingException extends Exception {


	private static final long serialVersionUID = -6674651877260880090L;

	public TypeLinkingException(String msg) {
		super(msg);
	}
	
	public static class NoSuchType extends TypeLinkingException {

		private static final long serialVersionUID = -7654756392748350710L;

		public NoSuchType(Identifier tid) {
			super(String.format("Unknown type %s.\n" +
					" at %s.", tid, tid.getPositionalString()));
		}
	}

	public static class AmbiguousType extends TypeLinkingException {

		private static final long serialVersionUID = -7654756392748350710L;

		public AmbiguousType(Identifier tid, TypeDecl... possibilities) {
			super(formatString(tid, possibilities));
		}
		
		private static String formatString(Identifier tid, TypeDecl... poss) {
			String msg = String.format("Type name %s is ambiguous in this context.\n"
				+ " at %s.\n Did you mean...", tid, tid.getPositionalString());
			for (TypeDecl decl : poss) {
				msg += String.format("\n -> %s (declared in %s)", 
						decl.getCanonicalName(), 
						decl.getPositionalString());
			}
			return msg;
		}
	}

	public static class NotRefType extends TypeLinkingException {
		private static final long serialVersionUID = -7654756392748350710L;

		public NotRefType(Type t, String positionalString) {
			super(String.format("Expected a reference type, found %s.\n" +
					" at %s.", t.getCanonicalName(), positionalString));
		}
	}
	

	public static class InstanceofPrimitive extends TypeLinkingException {
		private static final long serialVersionUID = -7654756392748350710L;

		public InstanceofPrimitive(Type t, String positionalString) {
			super(String.format("Expected a reference or array type, found %s.\n" +
					" at %s.", t.getCanonicalName(), positionalString));
		}
	}
}
