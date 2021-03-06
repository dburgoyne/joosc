package AbstractSyntax;

import CodeGeneration.AsmWriter;
import CodeGeneration.Frame;
import Exceptions.CodeGenerationException;
import Exceptions.ReachabilityException;
import Exceptions.TypeCheckingException;
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
	
	@Override public void checkReachability(boolean canLeavePrevious) throws ReachabilityException {
		// An expression can complete normally iff it is reachable.
		this.canEnter = canLeavePrevious;
		if (!this.canEnter) {
			throw new ReachabilityException.UnreachableStatement(this);
		}
		this.canLeave = this.canEnter;
	}
	
	public Object asConstExpr() { return null; }
	public final boolean isAlwaysTrue() { return Boolean.TRUE.equals(this.asConstExpr()); }
	public final boolean isAlwaysFalse() { return Boolean.FALSE.equals(this.asConstExpr()); }
	
	public void generateLValueCode(AsmWriter writer, Frame frame) throws CodeGenerationException {
		throw new CodeGenerationException.InvalidLValue(this);
	}
}
