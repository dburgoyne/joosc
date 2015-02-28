package AbstractSyntax;

import Parser.ParseTree;

public class ReturnStatement extends Statement {
	protected Expression expression;  // Could be null!
	
	public ReturnStatement(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("ReturnStatement"));
		
		if (tree.numChildren() == 2) {
			// Do nothing.
		} else if (tree.numChildren() == 3) {
			this.expression = Expression.extractExpression(tree.getChildren()[1]);
		}
	}
}
