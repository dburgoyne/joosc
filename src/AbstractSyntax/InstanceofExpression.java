package AbstractSyntax;

import Parser.ParseTree;
import Utilities.Cons;

public class InstanceofExpression extends Expression {
	
	protected Expression left;
	protected Identifier right;
	
	public InstanceofExpression(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("RelationalExpression"));
		
		this.left = Expression.extractExpression(tree.getChildren()[0]);
		this.right = new Identifier(tree.getChildren()[2]);
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException {
		this.environment = parentEnvironment;
		
		this.left.buildEnvironment(this.environment);
		this.right.buildEnvironment(this.environment);
	}
}
