package AbstractSyntax;

import Parser.ParseTree;
import Types.PrimitiveType;
import Types.Type;
import Utilities.Cons;

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
		
		if (tree.numChildren() == 1) {
			this.expression = Expression.extractExpression(tree.getChildren()[0]);
		} else if (tree.numChildren() == 2) {
			this.operator = UnaryOperator.fromString(tree.getChildren()[0].getSymbol());
			this.expression = Expression.extractExpression(tree.getChildren()[1]);
		}
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException, ImportException {
		this.environment = parentEnvironment;
		this.expression.buildEnvironment(this.environment);
	}

	@Override
	public void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException {
		this.expression.linkTypes(types);
	}

	@Override
	public void linkNames(TypeDecl curType, boolean staticCtx) throws NameLinkingException {
		this.expression.linkNames(curType, staticCtx);
	}

	@Override public void checkTypes() throws TypeCheckingException {
		this.expression.checkTypes();
		Type eType = this.expression.getType();
		if (this.operator == UnaryOperator.NOT
				&& !eType.equals(PrimitiveType.BOOLEAN)) {
			throw new TypeCheckingException.TypeMismatch(this.expression, "boolean");
		}
		if (this.operator == UnaryOperator.MINUS
				&& !(eType instanceof PrimitiveType
						&& ((PrimitiveType)eType).isIntegral())) {
			throw new TypeCheckingException.TypeMismatch(this.expression, "an integral type");
		}
		this.exprType = eType;
	}
}
