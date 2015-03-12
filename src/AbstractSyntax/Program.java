package AbstractSyntax;

import java.util.ArrayList;
import java.util.List;

import Parser.ParseTree;
import Utilities.Cons;

public class Program extends ASTNode {
	protected List<Classfile> files;
	
	// TODO Ew.
	public static TypeDecl javaLangObject;
	
	public Program(ParseTree... trees) {
		super(null);
		files = new ArrayList<Classfile>();
		for (ParseTree tree : trees) {
			Classfile file = new Classfile(tree);
			files.add(file);
		}
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException, ImportException {
		for (Classfile file : files) {
			EnvironmentDecl export = file.exportEnvironmentDecls();
			assert(export != null && export instanceof TypeDecl);
			
			// If we see java.lang.Object, remember where it is.
			TypeDecl type = (TypeDecl)export;
			if(type.getCanonicalName().equals("java.lang.Object")) {
				Program.javaLangObject = type;
			}
			
			this.environment = new Cons<EnvironmentDecl>(export, this.environment);
		}
		// If environment generation fails for one file, continue with the others,
		// then throw the original exception.
		Exception err = null;
		for (Classfile file : files) {
			try {
				file.buildEnvironment(this.environment);
			} catch (Exception caught) {
				if (err != null) {
					System.err.println(err);
				}
				err = caught;
			}
		}
		
		// Set java.lang.Object back to null to prevent explosion
		Program.javaLangObject = null;
		
		if (err != null) {
			if (err instanceof NameConflictException)
				throw (NameConflictException)err;
			else if (err instanceof ImportException)
				throw (ImportException)err;
			throw new RuntimeException(err);
		}
	}

	public EnvironmentDecl exportEnvironmentDecls() {
		// Do nothing.
		return null;
	}

	public void linkTypes(Cons<TypeDecl> allTypes) throws TypeLinkingException {
		Exception err = null;
		for (Classfile file : files) {
			try {
				
				file.linkTypes(allTypes);
				
			} catch (Exception caught) {
				if (err != null) {
					System.err.println(err);
				}
				err = caught;
			}
		}
		if (err != null) {
			if (err instanceof TypeLinkingException)
				throw (TypeLinkingException)err;
			throw new RuntimeException(err);
		}
	}
	
	public void linkNames(TypeDecl curType, boolean staticCtx) {
		Exception err = null;
		for (Classfile file : files) {
			try {
				
				file.linkNames(curType, staticCtx);
				
			} catch (Exception caught) {
				if (err != null) {
					System.err.println(err);
				}
				err = caught;
			}
		}
		if (err != null) {
			//if (err instanceof TypeLinkingException)
			//	throw (TypeLinkingException)err;
			throw new RuntimeException(err);
		}
	}
	
	// Re-export this.environment as a list of TypeDecls.
	public Cons<TypeDecl> getAllTypeDecls() {
		List<EnvironmentDecl> decls = Cons.toList(this.environment);
		Cons<TypeDecl> typeDecls = null;
		for (int i = decls.size() - 1; i >= 0; i--) {
			typeDecls = new Cons<TypeDecl>((TypeDecl)decls.get(i), typeDecls);
		}
		return typeDecls;
	}
}
