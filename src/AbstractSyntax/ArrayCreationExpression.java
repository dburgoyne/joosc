package AbstractSyntax;

import Parser.ParseTree;
import Utilities.Cons;

public class ArrayCreationExpression extends Expression {

	protected Identifier typeName;
	protected ArrayType type;
	
	protected Expression dimExpr;
	
	public ArrayCreationExpression(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("ArrayCreationExpression"));
		
		this.typeName = new Identifier(tree.getChildren()[1]);
		if (tree.numChildren() == 3) {
			this.dimExpr = Expression.extractExpression(tree.getChildren()[2].getChildren()[1]);
		} else if (tree.numChildren() == 4) {
			// Do nothing.
			// TODO The rules "ArrayCreationExpression new PrimitiveType [ ]" and
			// "ArrayCreationExpression new AmbiguousName [ ]" probably don't belong in Joos.
		}
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException, ImportException {
		this.environment = parentEnvironment;
		
		this.typeName.buildEnvironment(this.environment);
		this.dimExpr.buildEnvironment(this.environment);
	}
	
	@Override public void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException {
		Type type = this.typeName.resolveType(types, this.environment);
		this.type = new ArrayType(type);
		if (this.dimExpr != null) {
			this.dimExpr.linkTypes(types);
		}
	}
	
}
