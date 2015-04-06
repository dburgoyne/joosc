package AbstractSyntax;

import java.util.ArrayList;
import java.util.List;

import CodeGeneration.AsmWriter;
import CodeGeneration.Frame;
import Exceptions.ImportException;
import Exceptions.NameConflictException;
import Exceptions.NameLinkingException;
import Exceptions.ReachabilityException;
import Exceptions.TypeCheckingException;
import Exceptions.TypeLinkingException;
import Parser.ParseTree;
import Types.PrimitiveType;
import Utilities.Cons;

public class Program extends ASTNode {
	protected List<Classfile> files;
	
	public static TypeDecl javaLangObject;
	public static TypeDecl javaLangString;
	public static TypeDecl javaLangCloneable;
	public static TypeDecl javaIoSerializable;
	public static Method   staticIntTest;
	
	// List of all string literals in the program, to be populated before code generation.
	public static List<Literal> allStringLiterals; 
	
	public Program(ParseTree... trees) {
		super(null);
		files = new ArrayList<Classfile>();

		// Set java.lang.Object and java.lang.String back to null to prevent problems
		// during repeated runs.
		Program.javaLangObject = null;
		Program.javaLangString = null;
		Program.javaLangCloneable = null;
		Program.javaIoSerializable = null;
		Program.staticIntTest = null;
		Program.allStringLiterals = new ArrayList<Literal>();
		
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
			// Also need to remember java.lang.String to resolve types of string literals.
			TypeDecl type = (TypeDecl)export;
			if(type.getCanonicalName().equals("java.lang.Object")) {
				Program.javaLangObject = type;
			}
			if(type.getCanonicalName().equals("java.lang.String")) {
				Program.javaLangString = type;
			}
			if(type.getCanonicalName().equals("java.lang.Cloneable")) {
				Program.javaLangCloneable = type;
			}
			if(type.getCanonicalName().equals("java.io.Serializable")) {
				Program.javaIoSerializable = type;
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
	
	public void linkNames(TypeDecl curType, boolean staticCtx, EnvironmentDecl curDecl, Local curLocal, boolean lValue) throws NameLinkingException {
		Exception err = null;
		for (Classfile file : files) {
			try {
				
				file.linkNames(curType, staticCtx, curDecl, curLocal, false);
				
			} catch (Exception caught) {
				if (err != null) {
					System.err.println(err);
				}
				err = caught;
			}
		}
		if (err != null) {
			if (err instanceof NameLinkingException)
				throw (NameLinkingException)err;
			throw new RuntimeException(err);
		}
	}
	
	public void checkTypes() throws TypeCheckingException {
		Exception err = null;
		for (Classfile file : files) {
			try {
				
				file.checkTypes();
				
			} catch (Exception caught) {
				if (err != null) {
					System.err.println(err);
				}
				err = caught;
			}
		}
		if (err != null) {
			if (err instanceof TypeCheckingException)
				throw (TypeCheckingException)err;
			throw new RuntimeException(err);
		}
	}
	
	// Re-export this.environment as a list of TypeDecls.
	public Cons<TypeDecl> getAllTypeDecls() {
		List<EnvironmentDecl> decls = Cons.toList(this.environment);
		Cons<TypeDecl> typeDecls = null;
		for (int i = 0; i < decls.size(); i++) {
			typeDecls = new Cons<TypeDecl>((TypeDecl)decls.get(i), typeDecls);
		}
		return typeDecls;
	}
	
	@Override public void checkReachability(boolean canLeavePrevious) throws ReachabilityException {
		for (Classfile file : this.files) {
			file.checkReachability(true);
		}
	}
	
	// ---------- Code generation ----------
	
	@Override public void generateCode(AsmWriter writer, Frame frame) {
		Exception err = null;
		for (Classfile file : files) {
			try {
				writer = new AsmWriter(file.typeDecl);
				file.generateCode(writer, frame);
			} catch (Exception caught) {
				if (err != null) {
					System.err.println(err);
				}
				err = caught;
			} finally {
				if (writer != null) {
					writer.close();
				}
			}
		}
		if (err != null) {
			throw new RuntimeException(err);
		}
	}
	
	public static void generateStringTable(AsmWriter writer) {
		writer.pushComment("String literal table");

		writer.verbatimln("section .bss");
		for (int i = 0; i < Program.allStringLiterals.size(); i++) {
			Literal str = Program.allStringLiterals.get(i);
			writer.verbatimfn("global %s", str.label);
			writer.line("%s: resb %d",
					str.label,
					Program.javaLangString.sizeOf());
			writer.justDefinedGlobal(str.label);
		}
		
		writer.verbatimln("section .text");
		for (int i = 0; i < Program.allStringLiterals.size(); i++) {
			Literal str = Program.allStringLiterals.get(i);
			String label = str.label + "_data";
			writer.line("%s: db 0, %d, %d, %s",
					label,
					PrimitiveType.CHAR.getTypeID(),
					str.value.length() - 2,
					str.value);
		}
		
		// Call the java.lang.String constructor on each literal, and store the java.lang.String object references in an array.
		writer.verbatimfn("global strlit_init");
		writer.label("strlit_init");
		writer.justDefinedGlobal("strlit_init");
		
		for (int i = 0; i < Program.allStringLiterals.size(); i++) {
			Literal str = Program.allStringLiterals.get(i);
			// Push str.label
			writer.instr("push", str.label);
			// Push corresponding char[] literal label
			writer.instr("push", str.label + "_data");
			// Call constructor
			writer.instr("call", "ctor_java.lang.String#char@");
			// Callee pops.
		}
		writer.instr("ret");
		writer.popComment();
	}
	
	public static void generateStart(AsmWriter writer) {
		writer.pushComment("Program entry point");
		writer.verbatimln("global _start");
		writer.label("_start");
		
		// Call String Literal initializer
		writer.instr("call",  "strlit_init");
		writer.justUsedGlobal("strlit_init");
		
		// Call all static initializers
		for (TypeDecl ty : javaLangObject.allTypes) {
			String siLbl = ty.getInitializerLabel(true);
			writer.instr("call", siLbl);
			writer.justUsedGlobal(siLbl);
		}
		
		// Call static int test()
		String testLbl = staticIntTest.getDispatcherLabel();
		writer.instr("call", testLbl);
		writer.justUsedGlobal(testLbl);
		
		// call sys exit with eax.
		writer.instr("jmp", "__debexit"); // from runtime.s
		writer.justUsedGlobal("__debexit");
		
		writer.popComment();
	}
}
