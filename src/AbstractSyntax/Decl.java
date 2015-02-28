package AbstractSyntax;

import Parser.ParseTree;

public abstract class Decl extends ASTNode {

	protected Identifier type;
	protected Identifier name;
	
	public Decl(ParseTree tree, Identifier type, Identifier name) {
		super(tree);
		this.type = type;
		this.name = name;
	}
	
	public Decl(ParseTree tree) {
		super(tree);
	}

}
