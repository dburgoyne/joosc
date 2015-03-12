package AbstractSyntax;

import Parser.ParseTree;
import Utilities.Cons;

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
		ASSIGN;
		
		public static BinaryOperator fromString(String s) {
			return s.equals("+") ? PLUS
				 : s.equals("-") ? MINUS
				 : s.equals("*") ? STAR
				 : s.equals("/") ? SLASH
				 : s.equals("%") ? MOD
				 : s.equals("&") ? AND
				 : s.equals("&&") ? LAND
				 : s.equals("|") ? OR
				 : s.equals("||") ? LOR
				 : s.equals("^") ? XOR
				 : s.equals("==") ? EQ
				 : s.equals("!=") ? NE
				 : s.equals(">") ? GT
				 : s.equals("<") ? LT
				 : s.equals(">=") ? GE
				 : s.equals("<=") ? LE
				 : s.equals("=") ? ASSIGN
				 : null;
		}
	}
	
	protected Expression left, right;
	protected BinaryOperator operator;
	
	public BinaryExpression(ParseTree tree) {
		super(tree);
		this.left = Expression.extractExpression(tree.getChildren()[0]);
		this.operator = BinaryOperator.fromString(tree.getChildren()[1].getSymbol());
		this.right = Expression.extractExpression(tree.getChildren()[2]);
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException, ImportException {
		this.environment = parentEnvironment;
		
		this.left.buildEnvironment(this.environment);
		this.right.buildEnvironment(this.environment);
	}

	@Override
	public void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException {
		this.left.linkTypes(types);
		this.right.linkTypes(types);
	}

	@Override
	public void linkNames(TypeDecl curType, boolean staticCtx) throws NameLinkingException {
		this.left.linkNames(curType, staticCtx);
		this.right.linkNames(curType, staticCtx);
	}
}
