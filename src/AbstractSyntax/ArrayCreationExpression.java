package AbstractSyntax;

import Parser.ParseTree;

public class ArrayCreationExpression extends Expression {

	protected Identifier typeName;
	// TODO Fill this in during environment creation
	protected EnvironmentDecl type;
	
	protected Expression dimExpr;
	
	public ArrayCreationExpression(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("ArrayCreationExpression"));
		
		this.typeName = new Identifier(tree.getChildren()[1]);
		if (tree.numChildren() == 3) {
			this.dimExpr = Expression.extractExpression(tree.getChildren()[2].getChildren()[1]);
		} else if (tree.numChildren() == 4) {
			// Do nothing.
		}
	}
	
}
