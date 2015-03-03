package AbstractSyntax;

import java.util.ArrayList;
import java.util.List;

import Parser.ParseTree;
import Utilities.Cons;
import Utilities.Predicate;

public class Classfile extends ASTNode {
	
	protected Identifier packageName;
	protected List<Identifier> imports;
	protected TypeDecl typeDecl;

	public Classfile(ParseTree tree) {
		super(tree);
		
		assert(tree.getSymbol().equals("CompilationUnit"));
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
		typeDecl = new TypeDecl(tree.getChildren()[tree.numChildren()-1], this);
		
	}
	
	private void extractImports(ParseTree tree) {
		assert(tree.getSymbol().equals("ImportDeclarations"));
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
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException {
		// Don't inherit everything from the parent environment.
		this.environment = null;
		
		for (Identifier id : imports) {			
			final List<String> packageName = id.getPackageName();
			if (id.isStarImport()) {
				this.environment = Cons.filter(parentEnvironment,
					new Predicate<EnvironmentDecl>() {
						public boolean test(EnvironmentDecl decl) {
							if (!(decl instanceof TypeDecl)) return false;
							TypeDecl type = (TypeDecl)decl;
							return type.getPackageName().getComponents().equals(packageName);
						}
				});
			} else {
				final String typeName = id.getLastComponent();
				this.environment = Cons.filter(parentEnvironment,
						new Predicate<EnvironmentDecl>() {
							public boolean test(EnvironmentDecl decl) {
								if (!(decl instanceof TypeDecl)) return false;
								TypeDecl type = (TypeDecl)decl;
								return type.getPackageName().getComponents().equals(packageName)
								    && type.getName().equals(typeName);
							}
					});
			}
		}
		
		typeDecl.buildEnvironment(this.environment);
	}
	
	public EnvironmentDecl exportEnvironmentDecls() {
		return this.typeDecl.exportEnvironmentDecls();
	}
}
