package AbstractSyntax;

import Parser.ParseTree;

public class UnaryExpression extends Expression {

	enum UnaryOperator {
		NOT,
		MINUS;
		
		public static UnaryOperator fromString(String s) {
			return s.equals("!") ? NOT
				 : s.equals("-") ? MINUS
				 : null;
		}
	};
	
	protected UnaryOperator operator;
	protected Expression expression;
	
	public UnaryExpression(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("UnaryExpression")
			|| tree.getSymbol().equals("UnaryExpressionNotPlusMinus"));
		
		this.operator = UnaryOperator.fromString(tree.getChildren()[0].getSymbol());
		this.expression = Expression.extractExpression(tree.getChildren()[1]);
	}
}
