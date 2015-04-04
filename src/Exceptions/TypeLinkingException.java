package Exceptions;

import AbstractSyntax.Identifier;
import AbstractSyntax.TypeDecl;
import Types.Type;

public abstract class TypeLinkingException extends Exception {

	public static class PrefixMatchesType extends TypeLinkingException {

		private static final long serialVersionUID = 816989693605296449L;

		public PrefixMatchesType(Identifier id, TypeDecl type) {
			super(String.format("A prefix of %s matches the type %s.\n" +
					" at %s.", id, type.getName(), id.getPositionalString()));
		}
	
	}

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

	public static class BadSupertype extends TypeLinkingException {
		private static final long serialVersionUID = -7654756392748350710L;

		public BadSupertype(TypeDecl child, TypeDecl parent) {
			super(createMessage(child, parent));
		}
		
		private static String createMessage(TypeDecl child, TypeDecl parent) {
			String childKind = 
				child.getKind() == TypeDecl.Kind.CLASS ? "Class" : "Interface";
			String parentKind =
				parent.getKind() == TypeDecl.Kind.CLASS ? "class" : "interface";
			String verb = 
				child.getKind() == TypeDecl.Kind.CLASS
				  ? parent.getKind() == TypeDecl.Kind.CLASS
				      ? "implements"
					  : "extends"
				  : "extends";
			return String.format("%s %s %s %s %s\n at %s.",
					childKind, child.getCanonicalName(),
					verb,
					parentKind, parent.getCanonicalName(),
					child.getPositionalString());
		}
	}

	public static class AlreadyInherits extends TypeLinkingException {
		private static final long serialVersionUID = -7654756392748350710L;

		public AlreadyInherits(TypeDecl child, TypeDecl parent) {
			super(createMessage(child, parent));
		}
		
		private static String createMessage(TypeDecl child, TypeDecl parent) {
			String childKind = 
				child.getKind() == TypeDecl.Kind.CLASS ? "Class" : "Interface";
			String verb = 
				child.getKind() == TypeDecl.Kind.CLASS ? "implements" : "extends";
			return String.format("%s %s already %s interface %s\n at %s.",
					childKind, child.getCanonicalName(),
					verb,
					parent.getCanonicalName(),
					child.getPositionalString());
		}
	}
	
	public static class ExtendsFinal extends TypeLinkingException {
		private static final long serialVersionUID = -7654756392748350710L;

		public ExtendsFinal(TypeDecl child, TypeDecl parent) {
			super(String.format("Class %s extends final class %s\n at %s.",
					child.getCanonicalName(), parent.getCanonicalName(),
					child.getPositionalString()));
		}
	}
}
