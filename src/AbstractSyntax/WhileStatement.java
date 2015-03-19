package AbstractSyntax;

import Parser.ParseTree;
import Types.PrimitiveType;
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
	public void linkNames(TypeDecl curType, boolean staticCtx, EnvironmentDecl curDecl, Local curLocal, boolean lValue) throws NameLinkingException {
		this.condition.linkNames(curType, staticCtx, curDecl, curLocal, false);
		this.body.linkNames(curType, staticCtx, curDecl, curLocal, false);
	}

	@Override
	public void checkTypes() throws TypeCheckingException {
		this.condition.checkTypes();
		this.condition.assertNonVoid();
		
		if (this.condition.getType() != PrimitiveType.BOOLEAN) {
			throw new TypeCheckingException.TypeMismatch(this.condition, "boolean");
		}
		
		this.body.checkTypes();
	}
	@Override public void checkReachability(boolean canLeavePrevious) throws ReachabilityException {
		this.canEnter = canLeavePrevious;
		if (!this.canEnter) {
			throw new ReachabilityException.UnreachableStatement(this);
		}
		this.condition.checkReachability(true);
		
		// Body is unreachable if the condition is always false.
		this.body.checkReachability(!this.condition.isAlwaysFalse());
		
		// Can leave the loop only if the condition is not always true.
		this.canLeave = !this.condition.isAlwaysTrue();
	}
}
