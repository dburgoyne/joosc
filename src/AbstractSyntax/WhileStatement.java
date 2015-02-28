package AbstractSyntax;

import Parser.ParseTree;

public class WhileStatement extends Statement {
	
	protected Expression condition;
	protected Statement body;
	
	public WhileStatement(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("WhileStatement")
			|| tree.getSymbol().equals("WhileStatementNoShortIf"));
		
		this.condition = Expression.extractExpression(tree.getChildren()[2]);
		this.body = Statement.extractStatement(tree.getChildren()[4]);
	}
}
