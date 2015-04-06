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

public class InstanceofExpression extends Expression {
	
	protected Expression left;
	protected Identifier right;
	protected Type type;
	
	public InstanceofExpression(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("RelationalExpression"));
		
		this.left = Expression.extractExpression(tree.getChildren()[0]);
		this.right = new Identifier(tree.getChildren()[2]);
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException, ImportException {
		this.environment = parentEnvironment;
		
		this.left.buildEnvironment(this.environment);
		this.right.buildEnvironment(this.environment);
	}

	@Override
	public void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException {
		this.type = this.right.resolveType(types, this.environment);
		if (this.type instanceof PrimitiveType) {
			throw new TypeLinkingException.InstanceofPrimitive(type, 
					this.right.getPositionalString());
		}
		this.left.linkTypes(types);
	}

	@Override
	public void linkNames(TypeDecl curType, boolean staticCtx, EnvironmentDecl curDecl, Local curLocal, boolean lValue) throws NameLinkingException {
		this.left.linkNames(curType, staticCtx, curDecl, curLocal, false);
	}

	@Override
	public void checkTypes() throws TypeCheckingException {
		this.left.checkTypes();
		this.left.assertNonVoid();
		Type leftType = this.left.getType();
		
		if (!leftType.canBeCastAs(this.type)) {
			throw new TypeCheckingException.IllegalCast(leftType, this.right);
		}
		
		this.exprType = PrimitiveType.BOOLEAN;
	}
	
	// ---------- Code generation ----------
	
	@Override public void generateCode(AsmWriter writer, Frame frame) throws CodeGenerationException {
		
		String label = Utilities.Label.generateLabel("instanceof_end");
		
		this.left.generateCode(writer, frame);
		
		writer.instr("cmp", "eax", 0);
		writer.instr("je", label);
		
		if (this.type instanceof TypeDecl) {
			String stLabel = ((TypeDecl)this.type).getSubtypeTableLabel();
			writer.instr("mov", "eax", "[eax]"); // eax <- left.tid
			writer.instr("mov", "eax",           // eax <- V_(T, S)
					"[eax*4 + " + stLabel + "]");
			writer.justUsedGlobal(stLabel);
			writer.instr("cmp", "eax", 0);
			writer.instr("je", label);
		} else if (this.type instanceof ArrayType) {
			// left must have tid 0.
			writer.instr("mov", "ebx", "[eax]"); // ebx <- left.tid
			writer.instr("cmp", "ebx", this.type.getTypeID());
			writer.instr("jne", label);
			writer.instr("mov", "ebx", "[eax + 4]"); // ebx <- expression's inner primitive type's tid, or a pointer to its subtype table
			
			if (((ArrayType)this.type).getInnerType() instanceof PrimitiveType) {
				// Do an exact comparison of tids.
				writer.instr("mov", "eax", 0);
				writer.instr("cmp", "ebx", ((ArrayType)this.type).getInnerType().getTypeID());
				writer.instr("jne", label);
			} else {
				// Use the subtype table to compare types.
				assert(((ArrayType) this.type).getInnerType() instanceof TypeDecl);
				TypeDecl innerType = (TypeDecl)(((ArrayType) this.type).getInnerType());
				String innerTypeST = innerType.getSubtypeTableLabel();
				
				writer.instr("mov", "ebx", "[ebx-4]"); // ebx <- expression's inner reference type's tid
				writer.instr("mov", "eax",           // eax <- V_(T, S)
						"[ebx*4 + " + innerTypeST + "]");
				writer.justUsedGlobal(innerTypeST);
				writer.instr("cmp", "eax", 0);
				writer.instr("je", label);
			}
		}
		writer.instr("mov", "eax", 1);
		writer.label(label);
	}
}
