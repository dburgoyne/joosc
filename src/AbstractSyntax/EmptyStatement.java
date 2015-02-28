package AbstractSyntax;

import Parser.ParseTree;

public class EmptyStatement extends Statement {
	
	public EmptyStatement(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("EmptyStatement"));
		// Do nothing.
	}
	
}
