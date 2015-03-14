package AbstractSyntax;

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
}
