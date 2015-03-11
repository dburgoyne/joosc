package AbstractSyntax;

import Parser.ParseTree;
import Utilities.Cons;

public class ForStatement extends Statement {	
	
	protected BlockStatement initializer;
	protected Expression condition;
	protected Statement postExpression;
	protected Statement body;
	
	public ForStatement(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("ForStatement")
			|| tree.getSymbol().equals("ForStatementNoShortIf"));
		
		for (int index = 2; index < tree.numChildren() - 2; index++) {
			extractForLoopComponent(tree.getChildren()[index]);
		}
		this.body = Statement.extractStatement(tree.getChildren()[tree.numChildren()-1]);
	}
	
	private void extractForLoopComponent(ParseTree tree) {
		if (tree.getSymbol().equals("ForInit")) {
			this.initializer = BlockStatement.extractBlockStatement(tree);
		} else if (tree.getSymbol().equals("Expression")) {
			this.condition = Expression.extractExpression(tree);
		} else if (tree.getSymbol().equals("StatementExpression")) {
			this.postExpression = Expression.extractExpression(tree);
		} else if (tree.getSymbol().equals(";")) {
			// Do nothing.
		}
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException, ImportException {
		this.environment = parentEnvironment;
		
		if (this.initializer != null) {
			this.initializer.buildEnvironment(this.environment);
			EnvironmentDecl export = this.initializer.exportEnvironmentDecls();
			if (export != null) {
				this.environment = new Cons<EnvironmentDecl>(export, this.environment);
			}
		}
		if (this.condition != null) {
			this.condition.buildEnvironment(this.environment);
		}
		if (this.postExpression != null) {
			this.postExpression.buildEnvironment(this.environment);
		}
		assert(this.body != null);
		this.body.buildEnvironment(this.environment);
	}

	@Override public void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException {
		if (this.initializer != null) {
			this.initializer.linkTypes(types);
		}
		if (this.condition != null) {
			this.condition.linkTypes(types);
		}
		if (this.postExpression != null) {
			this.postExpression.linkTypes(types);
		}
		this.body.linkTypes(types);
	}

	@Override public void linkNames() throws NameLinkingException {
		if (this.initializer != null) {
			this.initializer.linkNames();
		}
		if (this.condition != null) {
			this.condition.linkNames();
		}
		if (this.postExpression != null) {
			this.postExpression.linkNames();
		}
		this.body.linkNames();
	}
}
