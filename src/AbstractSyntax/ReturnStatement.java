package AbstractSyntax;

import Parser.ParseTree;
import Utilities.Cons;

public class ReturnStatement extends Statement {
	protected Expression expression;  // Could be null!
	
	public ReturnStatement(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("ReturnStatement"));
		
		if (tree.numChildren() == 2) {
			// Do nothing.
		} else if (tree.numChildren() == 3) {
			this.expression = Expression.extractExpression(tree.getChildren()[1]);
		}
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException, ImportException {
		this.environment = parentEnvironment;
		this.expression.buildEnvironment(this.environment);
	}

	@Override
	public void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException {
		if (this.expression != null) {
			this.expression.linkTypes(types);
		}
	}
	
	@Override
	public void linkNames(TypeDecl curType, boolean staticCtx) throws NameLinkingException {
		if (this.expression != null) {
			this.expression.linkNames(curType, staticCtx);
		}
	}
}
