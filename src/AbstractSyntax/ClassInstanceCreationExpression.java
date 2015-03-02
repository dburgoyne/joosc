package AbstractSyntax;

import java.util.List;
import java.util.ArrayList;

import Parser.ParseTree;

public class ClassInstanceCreationExpression extends Expression {

	protected Identifier typeName;
	// TODO Fill this in during environment creation
	protected EnvironmentDecl type;
	
	protected List<Expression> arguments;
	
	public ClassInstanceCreationExpression(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("ClassInstanceCreationExpression"));
		
		this.typeName = new Identifier(tree.getChildren()[1]);
		if (tree.numChildren() == 4) {
			// Do nothing.
		} else if (tree.numChildren() == 5) {
			extractArgumentList(tree.getChildren()[3]);
		}
	}
	
	private void extractArgumentList(ParseTree tree) {
		assert(tree.getSymbol().equals("ArgumentList"));
		
		this.arguments = new ArrayList<Expression>();
		while (tree.numChildren() == 3) {
			Expression expr = Expression.extractExpression(tree.getChildren()[2]);
			this.arguments.add(0, expr);
		}
		Expression expr = Expression.extractExpression(tree.getChildren()[0]);
		this.arguments.add(0, expr);
	}
	
}
