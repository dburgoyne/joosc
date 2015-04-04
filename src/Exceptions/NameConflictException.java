package Exceptions;

import AbstractSyntax.Field;
import AbstractSyntax.Formal;
import AbstractSyntax.Local;
import AbstractSyntax.TypeDecl;

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
	
	public NameConflictException(Formal first, Formal second) {
		super(String.format("The formal paramater %s is already defined.\n"
				+ " at %s\n"
				+ "First defined at %s\n",
				second.getName().toString(),
				second.getPositionalString(),
				first.getPositionalString()));
	}

	public NameConflictException(Local first, Local second) {
		super(String.format("The local variable %s is already defined.\n"
				+ " at %s\n"
				+ "First defined at %s\n",
				second.getName().toString(),
				second.getPositionalString(),
				first.getPositionalString()));
	}

	public NameConflictException(Formal first, Local second) {
		super(String.format("The local variable %s shadows a formal parameter.\n"
				+ " at %s\n"
				+ "Formal parameter defined at %s\n",
				second.getName().toString(),
				second.getPositionalString(),
				first.getPositionalString()));
	}
}
