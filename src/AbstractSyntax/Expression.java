package AbstractSyntax;

import AbstractSyntax.Literal.LiteralType;
import Parser.ParseTree;
import Types.Type;

public abstract class Expression extends Statement {
	
	protected Type exprType;
	
	public Expression(ParseTree tree) {
		super(tree);
		// Do nothing.
	}
	
	public Type getType() {
		// Could return null! N.B.: we use null Type to mean void!
		// NullType is the type of the null literal.
		return this.exprType;
	}
	
	public void assertNonVoid() throws TypeCheckingException {
		if (this.exprType == null)
			throw new TypeCheckingException.TypeMismatch(this, "non-void");
	}

	public static Expression extractExpression(ParseTree tree) {
		//assert(tree.getSymbol().equals("Expression")
		//    || tree.getSymbol().equals("StatementExpression"));
		
		while (tree.numChildren() == 1
		   && !tree.getSymbol().equals("Primary")
		   && !tree.getSymbol().equals("AmbiguousName")) {
			tree = tree.getChildren()[0];
		}
		
		if (tree.getSymbol().equals("PrimaryNoNewArray")) {
			// ( Expression ) is the only non-unary PrimaryNoNewArray
			// Exists: ArrayAccess -> PrimaryNoNewArray [ Expression ]
			return Expression.extractExpression(tree.getChildren()[1]);
		}
		if (tree.getSymbol().equals("MethodInvocation")) {
			return new MethodInvocationExpression(tree);
		}
		if (tree.getSymbol().equals("ClassInstanceCreationExpression")) {
			return new ClassInstanceCreationExpression(tree);
		}
		if (tree.getSymbol().equals("FieldAccess")) {
			return new FieldAccessExpression(tree);
		}
		if (tree.getSymbol().equals("ArrayAccess")) {
			return new ArrayAccessExpression(tree);
		}
		if (tree.getSymbol().equals("AmbiguousName")) {
			return new Identifier(tree);
		}
		
		if (tree.numChildren() == 2) {
			return new UnaryExpression(tree);
		} else if (tree.numChildren() == 3) {
			if (tree.getChildren()[1].getSymbol().equals("instanceof")) {
				return new InstanceofExpression(tree);
			} else {
				return new BinaryExpression(tree);
			}
		} else {
			// Could be a CastExpression or a Primary.
			if (tree.getSymbol().equals("Primary")) {
				return extractPrimary(tree);
			} else { //if (tree.getSymbol().equals("CastExpression")) {
				return new CastExpression(tree);
			}
		}
	}
	
	public static Expression extractPrimary(ParseTree tree) {
		assert(tree.getSymbol().equals("Primary"));

		ParseTree firstChild = tree.getChildren()[0];
		if (firstChild.getSymbol().equals("PrimaryNoNewArray")) {
			ParseTree grandChild = firstChild.getChildren()[0];
			if (grandChild.getSymbol().equals("Literal")) {
				return new Literal(grandChild);
			}
			if (grandChild.getSymbol().equals("this")) {
				return new Identifier(grandChild);
			}
			if (grandChild.getSymbol().equals("MethodInvocation")) {
				return new MethodInvocationExpression(grandChild);
			}
			if (grandChild.getSymbol().equals("ArrayAccess")) {
				return new ArrayAccessExpression(grandChild);
			}
			if (grandChild.getSymbol().equals("FieldAccess")) {
				return new FieldAccessExpression(grandChild);
			}
			if (grandChild.getSymbol().equals("ClassInstanceCreationExpression")) {
				return new ClassInstanceCreationExpression(grandChild);
			}
			// Must be the rule "PrimaryNoNewArray ( Expression )"
			grandChild = firstChild.getChildren()[1];
			return Expression.extractExpression(grandChild);
		} else { //if (firstChild.getSymbol().equals("ArrayCreationExpression")) {
			return new ArrayCreationExpression(firstChild);
		}
		
	}
	
	@Override public void checkReachability(boolean canLeavePrevious) {
		// An expression can complete normally iff it is reachable.
		this.canEnter = canLeavePrevious;
		this.canLeave = this.canEnter;
	}
	
	// TODO TODO TODO
	public boolean isAlwaysTrue() { return false; }
	public boolean isAlwaysFalse() { return false; }
	public ExpressionValue tryFetchValue() { return null; }
	
	class ExpressionValue {
		Literal.LiteralType type;
		String value;
		
		public ExpressionValue(Literal.LiteralType type, String value) {
			this.type = type;
			this.value = value;
		}
		
		public void toNot() {
			assert(type.equals(LiteralType.BOOLEAN));
			if (value.equals("false")) {
				value = "true";
			} else {
				value = "false";
			}
		}
		
		public void toMinus() {
			assert(type.equals(LiteralType.INTEGER));
			value = Integer.toString(-1 * Integer.parseInt(value));
		}
		
		public boolean boolValue() {
			assert(type.equals(LiteralType.BOOLEAN));
			return value.equals("true");
		}
		
		public int intValue() {
			assert(type.equals(LiteralType.INTEGER));
			return Integer.parseInt(value);
		}
		
		public ExpressionValue binaryOperate(ExpressionValue ev, BinaryExpression.BinaryOperator op) {
			switch(op){
			case PLUS:
				assert(this.type.equals(LiteralType.INTEGER));
				assert(ev.type.equals(LiteralType.INTEGER));
				return new ExpressionValue(LiteralType.INTEGER, Integer.toString(Integer.parseInt(this.value) + Integer.parseInt(ev.value)));
			case MINUS:
				assert(this.type.equals(LiteralType.INTEGER));
				assert(ev.type.equals(LiteralType.INTEGER));
				return new ExpressionValue(LiteralType.INTEGER, Integer.toString(Integer.parseInt(this.value) - Integer.parseInt(ev.value)));
			case STAR:
				assert(this.type.equals(LiteralType.INTEGER));
				assert(ev.type.equals(LiteralType.INTEGER));
				return new ExpressionValue(LiteralType.INTEGER, Integer.toString(Integer.parseInt(this.value) * Integer.parseInt(ev.value)));
			case SLASH:
				assert(this.type.equals(LiteralType.INTEGER));
				assert(ev.type.equals(LiteralType.INTEGER));
				return new ExpressionValue(LiteralType.INTEGER, Integer.toString(Integer.parseInt(this.value) / Integer.parseInt(ev.value)));
			case MOD:
				assert(this.type.equals(LiteralType.INTEGER));
				assert(ev.type.equals(LiteralType.INTEGER));
				return new ExpressionValue(LiteralType.INTEGER, Integer.toString(Integer.parseInt(this.value) % Integer.parseInt(ev.value)));
			case LAND:
				assert(this.type.equals(LiteralType.BOOLEAN));
				assert(ev.type.equals(LiteralType.BOOLEAN));
				return new ExpressionValue(LiteralType.BOOLEAN, Boolean.toString(this.boolValue() && ev.boolValue()));
			case LOR:
				assert(this.type.equals(LiteralType.BOOLEAN));
				assert(ev.type.equals(LiteralType.BOOLEAN));
				return new ExpressionValue(LiteralType.BOOLEAN, Boolean.toString(this.boolValue() || ev.boolValue()));
			case EQ:
				assert(this.type.equals(LiteralType.INTEGER));
				assert(ev.type.equals(LiteralType.INTEGER));
				return new ExpressionValue(LiteralType.BOOLEAN, Boolean.toString(this.intValue() == ev.intValue()));
			case NE:
				assert(this.type.equals(LiteralType.INTEGER));
				assert(ev.type.equals(LiteralType.INTEGER));
				return new ExpressionValue(LiteralType.BOOLEAN, Boolean.toString(this.intValue() != ev.intValue()));
			case GT:
				assert(this.type.equals(LiteralType.INTEGER));
				assert(ev.type.equals(LiteralType.INTEGER));
				return new ExpressionValue(LiteralType.BOOLEAN, Boolean.toString(this.intValue() > ev.intValue()));
			case LT:
				assert(this.type.equals(LiteralType.INTEGER));
				assert(ev.type.equals(LiteralType.INTEGER));
				return new ExpressionValue(LiteralType.BOOLEAN, Boolean.toString(this.intValue() < ev.intValue()));
			case GE:
				assert(this.type.equals(LiteralType.INTEGER));
				assert(ev.type.equals(LiteralType.INTEGER));
				return new ExpressionValue(LiteralType.BOOLEAN, Boolean.toString(this.intValue() >= ev.intValue()));
			case LE:
				assert(this.type.equals(LiteralType.INTEGER));
				assert(ev.type.equals(LiteralType.INTEGER));
				return new ExpressionValue(LiteralType.BOOLEAN, Boolean.toString(this.intValue() <= ev.intValue()));
			case ASSIGN:
				assert(this.type.equals(ev.type));
				return ev;
			default:
				return null;
			}
		}
	}
}
