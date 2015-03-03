package AbstractSyntax;

import Parser.ParseTree;
import Utilities.Cons;

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
		
		this.condition = Expression.extractExpression(tree.getChildren()[2]);
		this.body = Statement.extractStatement(tree.getChildren()[4]);
		if (tree.getSymbol().equals("IfThenStatement")) {
			// No extra work
		} else if (tree.getSymbol().equals("IfThenElseStatement")
				|| tree.getSymbol().equals("IfThenElseStatementNoShortIf")) {
			this.elseBody = Statement.extractStatement(tree.getChildren()[6]);
		}
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException {
		this.environment = parentEnvironment;
		this.condition.buildEnvironment(this.environment);
		this.body.buildEnvironment(this.environment);
		if (this.elseBody != null) {
			this.elseBody.buildEnvironment(this.environment);
		}
	}
}
