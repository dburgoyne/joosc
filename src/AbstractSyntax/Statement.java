package AbstractSyntax;

import Parser.ParseTree;

public abstract class Statement extends BlockStatement {
	
	public Statement(ParseTree tree) {
		super(tree);
		// Do nothing
	}
	
	public static Statement extractStatement(ParseTree tree) {
		assert(tree.getSymbol().equals("Statement")
			|| tree.getSymbol().equals("StatementWithoutTrailingSubstatement")
			|| tree.getSymbol().equals("StatementNoShortIf"));
		
		while (tree.getSymbol().equals("Statement")
			|| tree.getSymbol().equals("StatementWithoutTrailingSubstatement")
			|| tree.getSymbol().equals("StatementNoShortIf")) {
			tree = tree.getChildren()[0];
		}
		
		if (tree.getSymbol().equals("IfThenStatement")
		 || tree.getSymbol().equals("IfThenElseStatement")
		 || tree.getSymbol().equals("IfThenElseStatementNoShortIf")) {
			return new IfStatement(tree);
		}
		if (tree.getSymbol().equals("WhileStatement")
		 || tree.getSymbol().equals("WhileStatementNoShortIf")) {
			return new WhileStatement(tree);
		}
		if (tree.getSymbol().equals("ForStatement")
		 || tree.getSymbol().equals("ForStatementNoShortIf")) {
			return new ForStatement(tree);
		}
		if (tree.getSymbol().equals("Block")) {
			return new Block(tree);
		}
		if (tree.getSymbol().equals("EmptyStatement")) {
			return new EmptyStatement(tree);
		}
		if (tree.getSymbol().equals("ExpressionStatement")) {
			return Expression.extractExpression(tree.getChildren()[0]);
		}
		//if (tree.getSymbol().equals("ReturnStatement")) {
			return new ReturnStatement(tree);
		//}
	}
	
	public EnvironmentDecl exportEnvironmentDecls() {
		return null;
	}
}
