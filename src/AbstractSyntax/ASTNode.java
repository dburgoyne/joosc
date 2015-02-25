package AbstractSyntax;

import java.util.List;

import Parser.ParseTree;
import Utilities.Cons;

public class ASTNode {
	protected ParseTree parseTree;
	protected List<ASTNode> children;
	
	protected Cons<Decl> environment;
	
	public ASTNode(ParseTree tree) {
		parseTree = tree;
	}
}
