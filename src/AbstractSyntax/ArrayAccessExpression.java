package AbstractSyntax;

import Parser.ParseTree;
import Types.ArrayType;
import Types.PrimitiveType;
import Types.Type;
import Utilities.Cons;

public class ArrayAccessExpression extends Expression {

	protected Expression array;
	protected Expression dimExpr;
	protected Type type;
	
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
	public void linkNames(TypeDecl curType, boolean staticCtx) throws NameLinkingException {
		assert this.array != null;
		this.array.linkNames(curType, staticCtx);
		this.dimExpr.linkNames(curType, staticCtx);
	}

	@Override public void checkTypes() throws TypeCheckingException {
		this.array.checkTypes();
		this.dimExpr.checkTypes();
		
		// array should be an ArrayType
		if (!(this.array.getType() instanceof ArrayType)) {
			throw new TypeCheckingException.TypeMismatch(this.array, "an array type");
		}
		
		// dimExpr should be an integral type.
		if (!(this.dimExpr.getType() instanceof PrimitiveType)
		 || ((PrimitiveType)this.dimExpr.getType()).isIntegral()) {
			throw new TypeCheckingException.TypeMismatch(this.dimExpr, "an integral type");
		}
		
		this.exprType = ((ArrayType)this.array.getType()).getInnerType();
	}	
}
