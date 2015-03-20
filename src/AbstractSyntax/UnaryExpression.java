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
	public void linkNames(TypeDecl curType, boolean staticCtx, EnvironmentDecl curDecl, Local curLocal, boolean lValue) throws NameLinkingException {
		this.expression.linkNames(curType, staticCtx, curDecl, curLocal, false);
	}

	@Override public void checkTypes() throws TypeCheckingException {
		this.expression.checkTypes();
		this.expression.assertNonVoid();
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
	
	@Override public Object asConstExpr() {
		Object subExpr = this.expression.asConstExpr();
		if (subExpr == null)
			return null;
		if (subExpr instanceof Boolean)
			return !((Boolean)subExpr);
		if (subExpr instanceof Byte)
			return -((Byte)subExpr);
		if (subExpr instanceof Short)
			return -((Short)subExpr);
		assert (subExpr instanceof Integer);
		return -((Integer)subExpr);
	}
}
