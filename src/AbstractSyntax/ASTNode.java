package AbstractSyntax;

import java.util.List;

import Parser.ParseTree;

public class ASTNode {
	protected ParseTree parseTree;
	protected List<ASTNode> children;
}
