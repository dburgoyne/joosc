package AbstractSyntax;

import Parser.ParseTree;

public class InstanceofExpression extends BinaryExpression {
	
	protected Expression left;
	protected Identifier right;
	
	public InstanceofExpression(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("RelationalExpression"));
		
		this.left = Expression.extractExpression(tree.getChildren()[0]);
		this.right = new Identifier(tree.getChildren()[2]);
	}
}
