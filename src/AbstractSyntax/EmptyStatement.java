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
}
