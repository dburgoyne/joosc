package AbstractSyntax;

import java.util.List;

import Parser.ParseTree;

public class Field extends Decl {

	protected List<Modifier> modifiers;
	protected Expression initializer;
	protected Identifier typeName;
	// TODO Fill this in during type resolution
	protected EnvironmentDecl type;
	
	public Field(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("FieldDeclaration"));
		
		this.modifiers = Modifier.extractModifiers(tree.getChildren()[0]);
		this.typeName = new Identifier(tree.getChildren()[1]);
		extractVariableDeclarator(tree.getChildren()[2]);
	}
	
	private void extractVariableDeclarator(ParseTree tree) {
		assert(tree.getSymbol().equals("VariableDeclarator"));
		extractVariableDeclaratorId(tree.getChildren()[0]);
		if (tree.numChildren() == 3) {
			extractVariableInitializer(tree.getChildren()[2]);
		}
	}

	private void extractVariableDeclaratorId(ParseTree tree) {
		assert(tree.getSymbol().equals("VariableDeclaratorId"));
		this.name = tree.getChildren()[0].getSymbol();
	}
	
	private void extractVariableInitializer(ParseTree tree) {
		assert(tree.getSymbol().equals("VariableInitializer"));
		this.initializer = Expression.extractExpression(tree.getChildren()[0]);
	}
}
