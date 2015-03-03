package AbstractSyntax;

import Parser.ParseTree;
import Utilities.Cons;

public class CastExpression extends Expression {
	
	protected Identifier type;
	protected Expression expression;
	
	public CastExpression(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("CastExpression"));
		
		this.type = new Identifier(tree.getChildren()[1]);
		this.expression = new UnaryExpression(tree.getChildren()[3]);
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException {
		this.environment = parentEnvironment;
		
		this.type.buildEnvironment(this.environment);
		this.expression.buildEnvironment(this.environment);
	}
}