package AbstractSyntax;

import Parser.ParseTree;
import Utilities.Cons;

public class ArrayAccessExpression extends Expression {

	protected Expression array;
	protected Expression dimExpr;
	
	public ArrayAccessExpression(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("ArrayAccess"));
		
		ParseTree firstChild = tree.getChildren()[0];
		if (firstChild.getSymbol().equals("ReferenceTypeNonArray")) {
			this.array = new Identifier(firstChild.getChildren()[0]);
		} else if (firstChild.getSymbol().equals("PrimaryNoNewArray")) {
			this.array = Expression.extractExpression(tree.getChildren()[0]);
		}
		
		this.dimExpr = Expression.extractExpression(tree.getChildren()[2]);
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException {
		this.environment = parentEnvironment;
		
		this.array.buildEnvironment(this.environment);
		this.dimExpr.buildEnvironment(this.environment);
	}
}
