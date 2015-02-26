package AbstractSyntax;

import java.util.ArrayList;
import java.util.List;

import Parser.ParseTree;

public class Identifier extends ASTNode {

	protected List<String> components;
	
	// Convenience functions.
	protected boolean isArray() {
		return components.get(components.size() - 1).equals("[]");
	}
	
	protected boolean isStarImport() {
		return components.get(components.size() - 1).equals("*");
	}
	
	public Identifier(ParseTree tree) {
		super(tree);
		components = new ArrayList<String>();
		while (tree.numChildren() > 1) {
			components.add(0, tree.getChildren()[2].getSymbol());
			tree = tree.getChildren()[0];
		}
		components.add(0, tree.getChildren()[0].getSymbol());
	}
}
