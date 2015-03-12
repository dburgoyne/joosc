package AbstractSyntax;

import AbstractSyntax.Identifier.Interpretation;
import Parser.ParseTree;
import Utilities.Cons;

public class FieldAccessExpression extends Expression implements Interpretation {

	protected Expression primary;
	protected String fieldName;
	
	// TODO Fill this in during type checking.
	protected Field field;

	public FieldAccessExpression(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("FieldAccess"));
		
		this.primary = Expression.extractPrimary(tree.getChildren()[0]);
		this.fieldName = new Identifier(tree.getChildren()[2]).getSingleComponent();
	}
	
	// Construct from an Identifier interpreted as a non-static field access.
	public FieldAccessExpression(Identifier id, Expression expr, String fieldName) {
		super(id.parseTree);
		
		this.primary = expr;
		this.fieldName = fieldName;
		this.environment = id.environment;
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException, ImportException {
		this.environment = parentEnvironment;
		this.primary.buildEnvironment(this.environment);
	}

	@Override public void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException {
		this.primary.linkTypes(types);
	}

	@Override public void linkNames(TypeDecl curType, boolean staticCtx) throws NameLinkingException {
		this.primary.linkNames(curType, staticCtx);
	}
}
