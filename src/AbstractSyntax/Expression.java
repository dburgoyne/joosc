package AbstractSyntax;

import Parser.ParseTree;

public abstract class Expression extends Statement {
	
	public Expression(ParseTree tree) {
		super(tree);
		// Do nothing.
	}

	public static Expression extractExpression(ParseTree tree) {
		//assert(tree.getSymbol().equals("Expression")
		//    || tree.getSymbol().equals("StatementExpression"));
		
		while (tree.numChildren() == 1
		   && !tree.getSymbol().equals("Primary")
		   && !tree.getSymbol().equals("AmbiguousName")) {
			tree = tree.getChildren()[0];
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
			// Could be a CastExpression, a Primary or an AmbiguousName.
			if (tree.getSymbol().equals("AmbiguousName")) {
				return new Identifier(tree);
			} else if (tree.getSymbol().equals("Primary")) {
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
			ParseTree ggChild = grandChild.getChildren()[1];
			return Expression.extractExpression(ggChild);
		} else { //if (firstChild.getSymbol().equals("ArrayCreationExpression")) {
			return new ArrayCreationExpression(firstChild);
		}
		
	}
}
