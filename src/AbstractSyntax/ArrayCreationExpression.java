package AbstractSyntax;

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
	
}
