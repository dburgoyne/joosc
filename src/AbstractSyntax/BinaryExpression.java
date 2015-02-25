package AbstractSyntax;

public class BinaryExpression extends Expression {
	
	enum BinaryOperator {
		PLUS,
		MINUS,
		STAR,
		SLASH,
		MOD,
		AND,
		LAND,
		OR,
		LOR,
		XOR,
		EQ,
		NE,
		GT,
		LT,
		GE,
		LE,
		ASSIGN,
		INSTANCEOF
	}
	
	protected Expression left, right;
	protected BinaryOperator operator;
}
