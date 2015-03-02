package AbstractSyntax;

import Parser.ParseTree;

public class CastExpression extends Expression {
	
	protected Identifier type;
	protected Expression expression;
	
	public CastExpression(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("CastExpression"));
		
		this.type = new Identifier(tree.getChildren()[1]);
		this.expression = new UnaryExpression(tree.getChildren()[3]);
	}
}