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
		assert(tree.getSymbol().equals("CompilationUnit"));
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
		assert(tree.getSymbol().equals("ImportDeclarations"));
		imports = new ArrayList<Identifier>();
		while (tree.getChildren().length > 1) {
			Identifier identifier = extractImport(tree.getChildren()[1]);
			imports.add(identifier);
			tree = tree.getChildren()[0];
		}
		Identifier identifier = extractImport(tree.getChildren()[0]);
		imports.add(identifier);
	}
	
	private static Identifier extractImport(ParseTree tree) {
		assert(tree.getSymbol().equals("ImportDeclaration"));
		String firstChildName = tree.getChildren()[0].getSymbol();
		Identifier identifier = new Identifier(tree.getChildren()[0].getChildren()[1]);
		if (firstChildName.equals("TypeImportOnDemandDeclaration")) {
			identifier.components.add("*");
		}
		return identifier;
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) {
		this.environment = parentEnvironment;
		
		for (Identifier id : imports) {
			// TODO Resolve all imports.
		}
		
		typeDecl.buildEnvironment(this.environment);
	}
	
	public List<EnvironmentDecl> exportEnvironmentDecls() {
		return this.typeDecl.exportEnvironmentDecls();
	}
}
