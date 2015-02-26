package AbstractSyntax;

import java.util.ArrayList;
import java.util.List;

import Parser.ParseTree;
import Utilities.Cons;

public class Classfile extends ASTNode {
	
	protected Identifier packageName;
	protected List<Identifier> imports;
	protected TypeDecl typeDecl;

	public Classfile(ParseTree tree) {
		super(tree);
		imports = new ArrayList<Identifier>();
		String firstChildName = tree.getChildren()[0].getSymbol();
		switch(tree.getChildren().length) {
		case 1:
			break;
		case 2:
			if (firstChildName.equals("PackageDeclaration")) {
				packageName = new Identifier(tree.getChildren()[0].getChildren()[1]);
			} else if (firstChildName.equals("ImportDeclarations")) {
				extractImports(tree.getChildren()[0]);
			}
			break;
		case 3:
			packageName = new Identifier(tree.getChildren()[0].getChildren()[1]);
			extractImports(tree.getChildren()[1]);
			break;
		}
		typeDecl = new TypeDecl(tree.getChildren()[tree.numChildren()-1]);
		
	}
	
	private void extractImports(ParseTree tree) {
		while (tree.getChildren().length > 1) {
			Identifier identifier = new Identifier(tree.getChildren()[1]);
			extractImport(tree.getChildren()[1], identifier);
			tree = tree.getChildren()[0];
			imports.add(identifier);
		}
		Identifier identifier = new Identifier(tree.getChildren()[0]);
		extractImport(tree.getChildren()[0], identifier);
		imports.add(identifier);
	}
	
	private static void extractImport(ParseTree tree, Identifier identifier) {
		String firstChildName = tree.getChildren()[0].getSymbol();
		extractAmbiguousName(tree.getChildren()[0].getChildren()[1], identifier);
		if (firstChildName.equals("TypeImportOnDemandDeclaration")) {
			identifier.components.add("*");
		}
	}

	private static void extractAmbiguousName(ParseTree tree, Identifier identifier) {
		while (tree.getChildren().length > 1) {
			identifier.components.add(0, tree.getChildren()[2].getSymbol());
			tree = tree.getChildren()[0];
		}
		identifier.components.add(0, tree.getChildren()[0].getSymbol());
	}
}
