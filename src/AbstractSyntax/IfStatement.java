package AbstractSyntax;

import Parser.ParseTree;

public class IfStatement extends Statement {
	
	protected Expression condition;
	protected Statement body;
	// TODO Consider whether a nullable elseBody is the best solution here.
	protected Statement elseBody;  // Could be null!
	
	public IfStatement(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("IfThenStatement")
		    || tree.getSymbol().equals("IfThenElseStatement")
			|| tree.getSymbol().equals("IfThenElseStatementNoShortIf"));
		
		this.condition = new Expression(tree.getChildren()[2]);
		this.body = Statement.extractStatement(tree.getChildren()[4]);
		if (tree.getSymbol().equals("IfThenStatement")) {
			// No extra work
		} else if (tree.getSymbol().equals("IfThenElseStatement")
				|| tree.getSymbol().equals("IfThenElseStatementNoShortIf")) {
			this.elseBody = Statement.extractStatement(tree.getChildren()[6]);
		}
		
	}
}
