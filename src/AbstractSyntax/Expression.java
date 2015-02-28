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
			} else if (tree.getSymbol().equals("CastExpression")) {
				return new CastExpression(tree);
			}
		}
	}
	
	public static Expression extractPrimary(ParseTree tree) {
		assert(tree.getSymbol().equals("Primary"));
		
		// TODO
	}
}
