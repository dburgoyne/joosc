package AbstractSyntax;

import CodeGeneration.AsmWriter;
import CodeGeneration.Frame;
import Exceptions.ReachabilityException;
import Exceptions.TypeCheckingException;
import Parser.ParseTree;
import Utilities.Cons;

public class EmptyStatement extends Statement {
	
	public EmptyStatement(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("EmptyStatement"));
		// Do nothing.
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) {
		this.environment = parentEnvironment;
	}

	@Override public void linkTypes(Cons<TypeDecl> types) { }	

	@Override public void linkNames(TypeDecl curType, boolean staticCtx, EnvironmentDecl curDecl, Local curLocal, boolean lValue) { }

	@Override public void checkTypes() throws TypeCheckingException { }
	
	@Override public void checkReachability(boolean canLeavePrevious) throws ReachabilityException {
		// An empty statement can complete normally iff it is reachable.
		this.canEnter = canLeavePrevious;
		if (!this.canEnter) {
			throw new ReachabilityException.UnreachableStatement(this);
		}
		this.canLeave = this.canEnter;
	}
	
	// ---------- Code generation ----------
	
	@Override public void generateCode(AsmWriter writer, Frame frame) {
		
		// Do nothing.
		writer.comment("Empty statement");
		
	}
}
