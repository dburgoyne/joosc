package AbstractSyntax;

import java.util.ArrayList;
import java.util.List;

import Parser.ParseTree;

public class Constructor extends ASTNode implements EnvironmentDecl {
	// The class this constructor belongs to.
	protected TypeDecl parent;
	protected List<Modifier> modifiers;
	protected List<Formal> parameters;
	protected List<BlockStatement> statements;
	
	public Constructor(ParseTree tree, TypeDecl parent) {
		super(tree);
		assert(tree.getSymbol().equals("ConstructorDeclaration"));
		this.parent = parent;
		
		if (tree.numChildren() == 2) {
			extractConstructorDeclarator(tree.getChildren()[0]);
			extractConstructorBody(tree.getChildren()[1]);
		} else if (tree.numChildren() == 3) {
			this.modifiers = Modifier.extractModifiers(tree.getChildren()[0]);
			extractConstructorDeclarator(tree.getChildren()[1]);
			extractConstructorBody(tree.getChildren()[2]);
		}
	}
	
	private void extractConstructorDeclarator(ParseTree tree) {
		assert(tree.getSymbol().equals("ConstructorDeclarator"));
		// We don't really need the name.
		// this.name = tree.getChildren()[0].getSymbol();
		parameters = new ArrayList<Formal>();
		if (tree.numChildren() == 3) {
			// Do nothing
		} else if (tree.numChildren() == 4) {
			Method.extractFormalParameterList(tree.getChildren()[2]);
		}
	}
	
	private void extractConstructorBody(ParseTree tree) {
		assert(tree.getSymbol().equals("ConstructorBody"));

		// TODO Parse the BlockStatements
	}
}
