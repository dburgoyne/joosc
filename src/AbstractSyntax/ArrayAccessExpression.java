package AbstractSyntax;

import Parser.ParseTree;

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
}
