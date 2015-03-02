package AbstractSyntax;

public class NameConflictException extends Exception {
	
	private static final long serialVersionUID = 8029510537961624081L;

	public NameConflictException(TypeDecl first, TypeDecl second) {
		super(String.format("The type %s is already defined.\n"
				+ " at %s\n"
				+ "First defined at %s\n",
				second.getCanonicalName().toString(),
				second.getPositionalString(),
				first.getPositionalString()));
	}

	public NameConflictException(Field first, Field second) {
		super(String.format("The field %s is already defined.\n"
				+ " at %s\n"
				+ "First defined at %s\n",
				second.getName().toString(),
				second.getPositionalString(),
				first.getPositionalString()));
	}
}
