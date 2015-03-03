package AbstractSyntax;

import Parser.ParseTree;
import Utilities.Cons;

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
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException {
		this.environment = parentEnvironment;
		this.condition.buildEnvironment(this.environment);
		this.body.buildEnvironment(this.environment);
	}
}
