package AbstractSyntax;

import java.util.ArrayList;
import java.util.List;

import Parser.ParseTree;

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
				flattenImports(tree.getChildren()[0], imports);
			}
			break;
		case 3:
			packageName = new Identifier(tree.getChildren()[0].getChildren()[1]);
			flattenImports(tree.getChildren()[1], imports);
			break;
		}
		typeDecl = new TypeDecl(tree.getChildren()[tree.numChildren()-1]);
		
	}
	
	private static void flattenImports(ParseTree tree, List<Identifier> list) {
		while (tree.getChildren().length > 1) {
			Identifier identifier = new Identifier(tree.getChildren()[1]);
			flattenImport(tree.getChildren()[1], identifier);
			tree = tree.getChildren()[0];
			list.add(identifier);
		}
		Identifier identifier = new Identifier(tree.getChildren()[0]);
		flattenImport(tree.getChildren()[0], identifier);
		list.add(identifier);
	}
	
	private static void flattenImport(ParseTree tree, Identifier identifier) {
		String firstChildName = tree.getChildren()[0].getSymbol();
		flattenAmbiguousName(tree.getChildren()[0].getChildren()[1], identifier);
		if (firstChildName.equals("TypeImportOnDemandDeclaration")) {
			identifier.components.add("*");
		}
	}

	private static void flattenAmbiguousName(ParseTree tree, Identifier identifier) {
		while (tree.getChildren().length > 1) {
			identifier.components.add(tree.getChildren()[2].getSymbol());
			tree = tree.getChildren()[0];
		}
		identifier.components.add(tree.getChildren()[0].getSymbol());
	}
}
