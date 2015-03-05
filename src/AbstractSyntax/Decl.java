package AbstractSyntax;

import Parser.ParseTree;

public abstract class Decl extends ASTNode implements EnvironmentDecl {

	protected Identifier typeName;
	protected Identifier name;
	
	public Decl(ParseTree tree, Identifier type, Identifier name) {
		super(tree);
		this.typeName = type;
		this.name = name;
	}
	
	public Decl(ParseTree tree) {
		super(tree);
	}

}
