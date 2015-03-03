package AbstractSyntax;

import Parser.ParseTree;
import Utilities.Cons;

public class FieldAccessExpression extends Expression {

	protected Expression primary;
	
	protected Identifier fieldName;
	// TODO Fill this in during environment creation
	protected Field field;
	
	public FieldAccessExpression(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("FieldAccess"));
		
		this.primary = Expression.extractPrimary(tree.getChildren()[0]);
		this.fieldName = new Identifier(tree.getChildren()[2]);
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException {
		this.environment = parentEnvironment;
		this.fieldName.buildEnvironment(this.environment);
		this.primary.buildEnvironment(this.environment);
	}
}
