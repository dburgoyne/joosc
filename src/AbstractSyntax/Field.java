package AbstractSyntax;

import java.util.ArrayList;
import java.util.List;

import Parser.ParseTree;

public class Field extends Decl {

	protected List<Modifier> modifiers;
	protected Expression initializer;
	
	// FieldDeclaration Modifiers Type VariableDeclarator ;
	// VariableDeclarator VariableDeclaratorId
	// VariableDeclarator VariableDeclaratorId = VariableInitializer
	
	public Field(ParseTree tree) {
		super(tree);
		
		this.modifiers = new ArrayList<Modifier>();
		
		extractModifiers(tree.getChildren()[0]);
		this.type = new Identifier(tree.getChildren()[1]);
		extractVariableDeclarator(tree.getChildren()[2]);
	}
	
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
	
	// Extracts elements from a VariableDeclarator node.
	private void extractVariableDeclarator(ParseTree tree) {
		this.name = new Identifier(tree.getChildren()[0]);
		if (tree.numChildren() > 1) {
			initializer = new Expression(tree.getChildren()[2]);
		}
	}
}
