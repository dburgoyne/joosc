package AbstractSyntax;

import Parser.ParseTree;

public class UnaryExpression extends Expression {

	enum UnaryOperator {
		NOT,
		MINUS
	};
	
	protected UnaryOperator operator;
	protected Expression expression;
	
	public UnaryExpression(ParseTree tree) {
		super(tree);
		if (tree.getChildren()[0].getSymbol().equals("-")) {
			operator = UnaryOperator.NOT;
		} else if (tree.getChildren()[0].getSymbol().equals("!")) {
			operator = UnaryOperator.MINUS;
		}
		
		this.expression = new Expression(tree.getChildren()[1]);
	}
}
