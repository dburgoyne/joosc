package AbstractSyntax;

import Parser.ParseTree;

public class Formal extends ASTNode {
	protected Identifier name;
	protected Expression initializer;
	protected Identifier typeName;
	// TODO Fill this in during type resolution
	protected EnvironmentDecl type;
	
	public Formal(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("FormalParameter"));
		this.typeName = new Identifier(tree.getChildren()[0]);
		extractVariableDeclaratorId(tree.getChildren()[1]);
	}
	
	private void extractVariableDeclaratorId(ParseTree tree) {
		assert(tree.getSymbol().equals("VariableDeclaratorId"));
		this.name = new Identifier(tree.getChildren()[0]);
	}
}
