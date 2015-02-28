package AbstractSyntax;

import java.util.ArrayList;
import java.util.List;

import Parser.ParseTree;

public class Block extends Statement {
	protected List<BlockStatement> statements;
	
	public Block(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("Block"));
		
		this.statements = new ArrayList<BlockStatement>();
		if (tree.numChildren() == 2) {
			// Do nothing
		} else if (tree.numChildren() == 3) {
			extractBlockStatements(tree.getChildren()[1]);
		}
	}
	
	private void extractBlockStatements(ParseTree tree) {
		assert(tree.getSymbol().equals("BlockStatements"));
		while (tree.numChildren() == 2) {
			BlockStatement bs = BlockStatement.extractBlockStatement(tree.getChildren()[1]);
			statements.add(bs);
			tree = tree.getChildren()[0];
		}
		BlockStatement bs = BlockStatement.extractBlockStatement(tree.getChildren()[0]);
		statements.add(bs);
	}
}
