package AbstractSyntax;

import Parser.ParseTree;

public class FieldAccessExpression extends Expression {

	protected Expression primary;
	
	protected Identifier fieldName;
	// TODO Fill this in during environment creation
	protected EnvironmentDecl field;
	
	public FieldAccessExpression(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("FieldAccess"));
		
		this.primary = Expression.extractPrimary(tree.getChildren()[0]);
		this.fieldName = new Identifier(tree.getChildren()[2]);
	}
}
