package AbstractSyntax;

public class UnaryExpression extends Expression {

	enum UnaryOperator {
		NOT,
		MINUS
	};
	
	protected UnaryOperator operator;
	protected Expression expression;
}
