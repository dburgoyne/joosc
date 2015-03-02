package AbstractSyntax;

import java.util.ArrayList;
import java.util.List;

import Parser.ParseTree;

public class MethodInvocationExpression extends Expression {

	protected Expression primary;
	
	protected Identifier methodName;
	// TODO Fill this in during environment creation
	protected EnvironmentDecl method;
	
	protected List<Expression> arguments;
	
	public MethodInvocationExpression(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("MethodInvocation"));
		
		if (tree.numChildren() == 3) {
			this.methodName = new Identifier(tree.getChildren()[0]);
		} else if (tree.numChildren() == 4) {
			this.methodName = new Identifier(tree.getChildren()[0]);
			extractArgumentList(tree.getChildren()[2]);
		} else if (tree.numChildren() == 5) {
			this.primary = Expression.extractExpression(tree.getChildren()[0]);
			this.methodName = new Identifier(tree.getChildren()[2]);
		} else if (tree.numChildren() == 6) {
			this.primary = Expression.extractExpression(tree.getChildren()[0]);
			this.methodName = new Identifier(tree.getChildren()[2]);
			extractArgumentList(tree.getChildren()[4]);
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
