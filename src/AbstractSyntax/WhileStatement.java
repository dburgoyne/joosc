package AbstractSyntax;

import Parser.ParseTree;
import Utilities.Cons;

public class WhileStatement extends Statement {
	
	protected Expression condition;
	protected Statement body;
	
	public WhileStatement(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("WhileStatement")
			|| tree.getSymbol().equals("WhileStatementNoShortIf"));
		
		this.condition = Expression.extractExpression(tree.getChildren()[2]);
		this.body = Statement.extractStatement(tree.getChildren()[4]);
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException, ImportException {
		this.environment = parentEnvironment;
		this.condition.buildEnvironment(this.environment);
		this.body.buildEnvironment(this.environment);
	}

	@Override
	public void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException {
		this.condition.linkTypes(types);
		this.body.linkTypes(types);
	}
	
	@Override
	public void linkNames(TypeDecl curType, boolean staticCtx) throws NameLinkingException {
		this.condition.linkNames(curType, staticCtx);
		this.body.linkNames(curType, staticCtx);
	}
}
