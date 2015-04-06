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
import Types.Type;
import Utilities.Cons;

public class ArrayCreationExpression extends Expression {

	protected Identifier typeName;
	protected ArrayType type;
	
	protected Expression dimExpr; //...can be null?
	
	public ArrayCreationExpression(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("ArrayCreationExpression"));
		
		this.typeName = new Identifier(tree.getChildren()[1]);
		if (tree.numChildren() == 3) {
			this.dimExpr = Expression.extractExpression(tree.getChildren()[2].getChildren()[1]);
		} else if (tree.numChildren() == 4) {
			// Do nothing.
			// The rules "ArrayCreationExpression new PrimitiveType [ ]" and
			// "ArrayCreationExpression new AmbiguousName [ ]" probably don't belong in Joos.
		}
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException, ImportException {
		this.environment = parentEnvironment;
		
		this.typeName.buildEnvironment(this.environment);
		if (this.dimExpr != null) {
			this.dimExpr.buildEnvironment(this.environment);
		}
	}
	
	@Override public void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException {
		Type type = this.typeName.resolveType(types, this.environment);
		this.type = new ArrayType(type);
		if (this.dimExpr != null) {
			this.dimExpr.linkTypes(types);
		}
	}
	
	@Override public void linkNames(TypeDecl curType, boolean staticCtx, EnvironmentDecl curDecl, Local curLocal, boolean lValue) throws NameLinkingException {
		if (this.dimExpr != null) {
			this.dimExpr.linkNames(curType, staticCtx, curDecl, curLocal, false);
		}
	}

	@Override
	public void checkTypes() throws TypeCheckingException {
		
		// TO DO This should probably be fixed in the grammar.
		if (this.dimExpr == null) {
			throw new TypeCheckingException.TypeMismatch(this, "an integral type");
		}

		this.dimExpr.checkTypes();
		this.dimExpr.assertNonVoid();
		
		if (!(this.dimExpr.getType() instanceof PrimitiveType
				&& ((PrimitiveType)this.dimExpr.getType()).isIntegral())) {
			throw new TypeCheckingException.TypeMismatch(this.dimExpr, "an integral type");
		}
		
		this.exprType = this.type;
	}
	
	// ---------- Code generation ----------
	
	@Override public void generateCode(AsmWriter writer, Frame frame) throws CodeGenerationException {
		// Determine object size.
		this.dimExpr.generateCode(writer, frame);
		writer.instr("push", "eax");  // Remember eax so we can loop later
		writer.instr("lea", "eax", "[eax * 4 + 12]");
		// Call __malloc
		writer.instr("call", "__malloc");
		writer.justUsedGlobal("__malloc");
		writer.instr("pop", "ecx");  // eax holds the number of elements
		writer.instr("push", "eax");
		// Set the array type ID (0) in the first dword.
		writer.instr("mov", "[eax]", "dword " + this.type.getTypeID());
		// Set the inner (type ID / subtype table) in the second dword.
		{
			String entry = null;
			if (this.type.getInnerType() instanceof PrimitiveType) {
				entry = String.valueOf(this.type.getInnerType().getTypeID());
			} else if (this.type.getInnerType() instanceof TypeDecl) {
				entry = ((TypeDecl)this.type.getInnerType()).getSubtypeTableLabel();
				writer.justUsedGlobal(entry);
			} else {
				assert false;
			}
			writer.instr("mov", "[eax + 4]", "dword " + entry);
		}
		// Set the array length in the third dword.
		writer.instr("mov", "[eax + 8]", "ecx");
		// Zero the object
		String startLabel = Utilities.Label.generateLabel("loop_start");
		String endLabel = Utilities.Label.generateLabel("loop_end");
		writer.instr("add", "eax", "12");
		writer.instr("cmp", "ecx", "0");
		writer.instr("je", endLabel);
		writer.label(startLabel);
		writer.instr("mov", "[eax]", "dword 0");
		writer.instr("add", "eax", "4");
		writer.instr("loop", startLabel);
		writer.label(endLabel);
		// Pop the address of the new object.
		writer.instr("pop", "eax");
	}
}
