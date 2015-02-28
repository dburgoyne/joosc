package AbstractSyntax;

import Parser.ParseTree;

public class Local extends BlockStatement implements EnvironmentDecl {
	
	protected Identifier typeName;
	// TODO Fill this is during type resolution.
	protected EnvironmentDecl type;
	protected Identifier name;
	protected Expression initializer;  // Never null!
	
	public Identifier getName() {
		return name;
	}
	
	public Local(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("LocalVariableDeclaration")
			|| tree.getSymbol().equals("LocalVariableDeclarationStatement"));
		
		if (tree.getSymbol().equals("LocalVariableDeclarationStatement")) {
			tree = tree.getChildren()[0];
		}
		assert(tree.getSymbol().equals("LocalVariableDeclaration"));
		
		this.typeName = new Identifier(tree.getChildren()[0]);
		ParseTree secondChild = tree.getChildren()[1];
		this.name = new Identifier(secondChild.getChildren()[0].getChildren()[0]);
		this.initializer = Expression.extractExpression(secondChild.getChildren()[2].getChildren()[0]);
		
	}

}
