package AbstractSyntax;

import Parser.ParseTree;
import Types.PrimitiveType;
import Utilities.Cons;

public class IfStatement extends Statement {
	
	protected Expression condition;
	protected Statement body;
	protected Statement elseBody;  // Could be null!
	
	public IfStatement(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("IfThenStatement")
		    || tree.getSymbol().equals("IfThenElseStatement")
			|| tree.getSymbol().equals("IfThenElseStatementNoShortIf"));
		
		this.condition = Expression.extractExpression(tree.getChildren()[2]);
		this.body = Statement.extractStatement(tree.getChildren()[4]);
		if (tree.getSymbol().equals("IfThenStatement")) {
			// No extra work
		} else if (tree.getSymbol().equals("IfThenElseStatement")
				|| tree.getSymbol().equals("IfThenElseStatementNoShortIf")) {
			this.elseBody = Statement.extractStatement(tree.getChildren()[6]);
		}
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException, ImportException {
		this.environment = parentEnvironment;
		this.condition.buildEnvironment(this.environment);
		this.body.buildEnvironment(this.environment);
		if (this.elseBody != null) {
			this.elseBody.buildEnvironment(this.environment);
		}
	}

	@Override
	public void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException {
		this.condition.linkTypes(types);
		this.body.linkTypes(types);
		if (this.elseBody != null)
			this.elseBody.linkTypes(types);
	}
	
	@Override
	public void linkNames(TypeDecl curType, boolean staticCtx, EnvironmentDecl curDecl, Local curLocal, boolean lValue) throws NameLinkingException {
		this.condition.linkNames(curType, staticCtx, curDecl, curLocal, false);
		this.body.linkNames(curType, staticCtx, curDecl, curLocal, false);
		if (this.elseBody != null)
			this.elseBody.linkNames(curType, staticCtx, curDecl, curLocal, false);
	}

	@Override
	public void checkTypes() throws TypeCheckingException {
		this.condition.checkTypes();
		this.condition.assertNonVoid();
		
		if (this.condition.getType() != PrimitiveType.BOOLEAN) {
			throw new TypeCheckingException.TypeMismatch(this.condition, "boolean");
		}
		
		this.body.checkTypes();
		if (this.elseBody != null) {
			this.elseBody.checkTypes();
		}
	}
	
	@Override public void checkReachability(boolean canLeavePrevious) throws ReachabilityException {
		this.canEnter = canLeavePrevious;
		if (!this.canEnter) {
			throw new ReachabilityException.UnreachableStatement(this);
		}
		this.condition.checkReachability(true);  // Not really necessary
		this.body.checkReachability(true);
		if (this.elseBody == null) {
			this.canLeave = this.canEnter;
		} else {
			this.elseBody.checkReachability(true);
			this.canLeave = this.body.canLeave || this.elseBody.canLeave;
		}
	}
}
