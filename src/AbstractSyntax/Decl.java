package AbstractSyntax;

import Parser.ParseTree;

public abstract class Decl extends ASTNode {

	public Decl(ParseTree tree) {
		super(tree);
	}
	
	protected Identifier type;
	protected String name;

}
