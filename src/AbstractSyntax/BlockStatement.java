package AbstractSyntax;

import Parser.ParseTree;

public abstract class BlockStatement extends ASTNode {
	
	public BlockStatement(ParseTree tree) {
		super(tree);
		// Do nothing
	}

	public static BlockStatement extractBlockStatement(ParseTree tree) {
		assert(tree.getSymbol().equals("BlockStatement")
			|| tree.getSymbol().equals("ForInit"));
		ParseTree firstChild = tree.getChildren()[0];
		if (firstChild.getSymbol().equals("LocalVariableDeclarationStatement")
		 || firstChild.getSymbol().equals("LocalVariableDeclaration")) {
			return new Local(firstChild);
		} else if (firstChild.getSymbol().equals("Statement")) {
			return Statement.extractStatement(firstChild);
		} else { //if (firstChild.getSymbol().equals("StatementExpression")) {
			return Expression.extractExpression(firstChild);
		}
	}
}
