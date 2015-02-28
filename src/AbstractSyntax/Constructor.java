package AbstractSyntax;

import java.util.List;

import Parser.ParseTree;

public class Constructor extends ASTNode {
	protected Modifier modifiers;
	protected List<Formal> parameters;
	protected List<BlockStatement> statements;
	
	// ConstructorDeclaration ConstructorDeclarator ConstructorBody
	// ConstructorDeclaration Modifiers ConstructorDeclarator ConstructorBody
	
	// Extracts modifiers from a Modifiers node.
	private void extractModifiers(ParseTree tree) {
		while (tree.getChildren().length > 1) {
			Modifier modifier = Modifier.fromString(tree.getChildren()[1].getSymbol());
			this.modifiers.add(modifier);
			tree = tree.getChildren()[0];
		}
		Modifier modifier = Modifier.fromString(tree.getChildren()[0].getSymbol());
		this.modifiers.add(modifier);
	}
}
