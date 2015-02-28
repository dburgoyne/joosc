package AbstractSyntax;

import Parser.ParseTree;

public class ForStatement extends Statement {	
	
	protected BlockStatement initializer;
	protected Expression condition;
	protected Statement postExpression;
	protected Statement body;
	
	public ForStatement(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("ForStatement")
			|| tree.getSymbol().equals("ForStatementNoShortIf"));
		
		for (int index = 2; index < tree.numChildren() - 2; index++) {
			extractForLoopComponent(tree.getChildren()[index]);
		}
		this.body = Statement.extractStatement(tree.getChildren()[tree.numChildren()-1]);
	}
	
	private void extractForLoopComponent(ParseTree tree) {
		if (tree.getSymbol().equals("ForInit")) {
			this.initializer = BlockStatement.extractBlockStatement(tree.getChildren()[0]);
		} else if (tree.getSymbol().equals("Expression")) {
			this.condition = Expression.extractExpression(tree);
		} else if (tree.getSymbol().equals("StatementExpression")) {
			this.postExpression = Expression.extractExpression(tree);
		} else if (tree.getSymbol().equals(";")) {
			// Do nothing.
		}
	}
}
