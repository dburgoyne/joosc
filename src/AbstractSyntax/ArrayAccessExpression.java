package AbstractSyntax;

import Parser.ParseTree;

public class ArrayAccessExpression extends Expression {

	protected Expression array;
	protected Expression dimExpr;
	
	public ArrayAccessExpression(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("ArrayAccess"));
		
		this.array = Expression.extractExpression(tree.getChildren()[0]);
		this.dimExpr = Expression.extractExpression(tree.getChildren()[2]);
	}
}
