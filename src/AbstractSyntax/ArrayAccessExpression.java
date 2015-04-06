package AbstractSyntax;

import CodeGeneration.AsmWriter;
import CodeGeneration.Frame;
import Exceptions.CodeGenerationException;
import Exceptions.ImportException;
import Exceptions.NameConflictException;
import Exceptions.NameLinkingException;
import Exceptions.TypeCheckingException;
import Exceptions.TypeLinkingException;
import Parser.ParseTree;
import Types.ArrayType;
import Types.PrimitiveType;
import Utilities.Cons;

public class ArrayAccessExpression extends Expression {

	protected Expression array;
	protected Expression dimExpr;
	
	public ArrayAccessExpression(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("ArrayAccess"));
		
		ParseTree firstChild = tree.getChildren()[0];
		if (firstChild.getSymbol().equals("ReferenceTypeNonArray")) {
			this.array = new Identifier(firstChild.getChildren()[0]);
		} else if (firstChild.getSymbol().equals("PrimaryNoNewArray")) {
			this.array = Expression.extractExpression(tree.getChildren()[0]);
		}
		
		this.dimExpr = Expression.extractExpression(tree.getChildren()[2]);
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException, ImportException {
		this.environment = parentEnvironment;
		
		this.array.buildEnvironment(this.environment);
		this.dimExpr.buildEnvironment(this.environment);
	}

	@Override
	public void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException {
		assert this.array != null;
		this.array.linkTypes(types);
		this.dimExpr.linkTypes(types);
	}
	
	@Override
	public void linkNames(TypeDecl curType, boolean staticCtx, EnvironmentDecl curDecl, Local curLocal, boolean lValue) throws NameLinkingException {
		assert this.array != null;
		this.array.linkNames(curType, staticCtx, curDecl, curLocal, false);
		this.dimExpr.linkNames(curType, staticCtx, curDecl, curLocal, false);
	}

	@Override public void checkTypes() throws TypeCheckingException {
		this.array.checkTypes();
		this.array.assertNonVoid();
		this.dimExpr.checkTypes();
		this.dimExpr.assertNonVoid();
		
		// array should be an ArrayType
		if (!(this.array.getType() instanceof ArrayType)) {
			throw new TypeCheckingException.TypeMismatch(this.array, "an array type");
		}
		
		// dimExpr should be an integral type.
		if (!(this.dimExpr.getType() instanceof PrimitiveType
		      && ((PrimitiveType)this.dimExpr.getType()).isIntegral())) {
			throw new TypeCheckingException.TypeMismatch(this.dimExpr, "an integral type");
		}
		
		this.exprType = ((ArrayType)this.array.getType()).getInnerType();
	}	
	
	// ---------- Code generation ----------
	
	private void generateCommon(AsmWriter writer, Frame frame, String instr) throws CodeGenerationException {
		this.array.generateCode(writer, frame); // eax <- the array
		writer.instr("cmp", "eax", 0); // fail if array is null
		writer.instr("je",    "__exception");
		writer.justUsedGlobal("__exception");
		writer.instr("push", "dword [eax+4]"); // push inner type's subtype table
		writer.instr("push", "eax");     // push the array object.
		
		this.dimExpr.generateCode(writer, frame); // eax <- index
		writer.instr("cmp", "eax", 0); // fail if index < 0
		writer.instr("jl",    "__exception");
		writer.justUsedGlobal("__exception");
		
		writer.instr("pop", "ebx"); // ebx <- array object
		writer.instr("cmp", "eax", "[ebx + 8]"); // fail if index >= array length
		writer.instr("jge", "__exception");
		
		writer.instr(instr, "eax", "[ebx + 12 + 4*eax]"); // eax <- ebx[eax]
		writer.instr("pop", "ebx");  // ebx <- array's inner type's subtype table
	}
	
	@Override public void generateCode(AsmWriter writer, Frame frame) throws CodeGenerationException {
		this.generateCommon(writer, frame, "mov");
	}
	
	@Override public void generateLValueCode(AsmWriter writer, Frame frame) throws CodeGenerationException {
		this.generateCommon(writer, frame, "lea");
	}
}
