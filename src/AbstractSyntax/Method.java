package AbstractSyntax;

import java.util.ArrayList;
import java.util.List;

import Parser.ParseTree;

public class Method extends Decl {
	protected List<Modifier> modifiers;
	protected List<Formal> parameters;
	
	protected Block statements;
	
	// MethodDeclaration MethodHeader MethodBody
	
	// MethodHeader Modifiers Type MethodDeclarator
	// MethodHeader Modifiers void MethodDeclarator

	// MethodDeclarator Identifier ( )
	// MethodDeclarator Identifier ( FormalParameterList )
	
	// FormalParameterList FormalParameter
	// FormalParameterList FormalParameterList , FormalParameter

	// FormalParameter Type VariableDeclaratorId
	
	// Modifiers Modifier
	// Modifiers Modifiers Modifier
	
	// AbstractMethodDeclaration Type MethodDeclarator ;
	// AbstractMethodDeclaration void MethodDeclarator ;
	// AbstractMethodDeclaration Modifiers Type MethodDeclarator ;
	// AbstractMethodDeclaration Modifiers void MethodDeclarator ;
	
	public Method(ParseTree tree) {
		super(tree);
		
		this.modifiers = new ArrayList<Modifier>();
		this.parameters = new ArrayList<Formal>();
		
	}
	
	// Extracts elements from a MethodHeader node.
	private void extractMethodHeader(ParseTree tree) {
		extractModifiers(tree.getChildren()[0]);
		this.type = new Identifier(tree.getChildren()[1]);
		extractMethodDeclarator(tree.getChildren()[2]);
	}
	
	// Extracts elements from a AbstractMethodDeclaration node.
	private void extractAbstractMethodDeclaration(ParseTree tree) {
		if(tree.numChildren() > 2){
			extractModifiers(tree.getChildren()[0]);
			this.type = new Identifier(tree.getChildren()[1]);
			extractMethodDeclarator(tree.getChildren()[2]);
		} else {
			this.type = new Identifier(tree.getChildren()[0]);
			extractMethodDeclarator(tree.getChildren()[1]);
		}
	}	
	
	// Extracts elements from a MethodDeclarator node.
	private void extractMethodDeclarator(ParseTree tree) {
		this.name=new Identifier(tree.getChildren()[0]);
		if(tree.numChildren() > 3){
			extractParameters(tree.getChildren()[2]);
		}
	}
	
	// Extracts elements from a FormalParameterList node.
	private void extractParameters(ParseTree tree){
		while (tree.getChildren().length > 1) {
			Formal formal = new Formal(tree.getChildren()[2]);
			this.parameters.add(formal);
			tree = tree.getChildren()[0];
		}
		Formal formal = new Formal(tree.getChildren()[0]);
		this.parameters.add(formal);
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
}
