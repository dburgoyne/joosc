package AbstractSyntax;

import Parser.ParseTree;
import Utilities.Cons;

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
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException {
		this.environment = parentEnvironment;
		this.expression.buildEnvironment(this.environment);
	}
}
