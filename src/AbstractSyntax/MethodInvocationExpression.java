package AbstractSyntax;

import java.util.ArrayList;
import java.util.List;

import Parser.ParseTree;
import Utilities.Cons;

public class MethodInvocationExpression extends Expression {

	protected Expression primary; // Can be null !!!
	
	protected Identifier methodName;
	// TODO Fill this in during name resolution.
	protected Method method;
	
	protected List<Expression> arguments;
	
	public MethodInvocationExpression(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("MethodInvocation"));
		
		this.arguments = new ArrayList<Expression>();
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
		
		while (tree.numChildren() == 3) {
			Expression expr = Expression.extractExpression(tree.getChildren()[2]);
			this.arguments.add(0, expr);
			tree = tree.getChildren()[0];
		}
		Expression expr = Expression.extractExpression(tree.getChildren()[0]);
		this.arguments.add(0, expr);
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException, ImportException {
		this.environment = parentEnvironment;
		if (this.primary != null) {
			this.primary.buildEnvironment(this.environment);
		}
		this.methodName.buildEnvironment(this.environment);
		for (Expression argument : this.arguments) {
			argument.buildEnvironment(this.environment);
		}
	}

	@Override
	public void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException {
		if (this.primary != null) {
			this.primary.linkTypes(types);
		}
		for (Expression arg : this.arguments) {
			arg.linkTypes(types);
		}
	}
}
