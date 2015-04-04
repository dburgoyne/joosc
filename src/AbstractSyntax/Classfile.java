package AbstractSyntax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import CodeGeneration.AsmWriter;
import CodeGeneration.Frame;
import Exceptions.ImportException;
import Exceptions.NameConflictException;
import Exceptions.NameLinkingException;
import Exceptions.ReachabilityException;
import Exceptions.TypeCheckingException;
import Exceptions.TypeLinkingException;
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
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws ImportException, NameConflictException {
		
		class MatchesPackage implements Predicate<EnvironmentDecl> {
			List<String> packageName;
			public MatchesPackage(List<String> packageName) {
				this.packageName = packageName;
			}
			public boolean test(EnvironmentDecl decl) {
				if (!(decl instanceof TypeDecl)) return false;
				TypeDecl type = (TypeDecl)decl;
				Identifier typePackageName = type.getPackageName();
				return typePackageName == null 
					 ? packageName.isEmpty() 
					 : typePackageName.getComponents().equals(packageName);
			}
		}
		
		class MatchesPackageOrPrefix implements Predicate<EnvironmentDecl> {
			List<String> packageName;
			public MatchesPackageOrPrefix(List<String> packageName) {
				this.packageName = packageName;
			}
			private boolean matchesPrefix(List<String> prefix, List<String> packageName) {
				if (prefix.size() > packageName.size()) return false;
				for (int i = 0; i < prefix.size(); i++) {
					if (!prefix.get(i).equals(packageName.get(i))) return false;
				}
				return true;
			}
			public boolean test(EnvironmentDecl decl) {
				if (!(decl instanceof TypeDecl)) return false;
				TypeDecl type = (TypeDecl)decl;
				Identifier typePackageName = type.getPackageName();
				return typePackageName == null 
					 ? packageName.isEmpty() 
					 : matchesPrefix(this.packageName, typePackageName.getComponents());
			}
		}
		
		class MatchesSimpleName implements Predicate<EnvironmentDecl> {
			String simpleName;
			public MatchesSimpleName(String simpleName) {
				this.simpleName = simpleName;
			}

			public boolean test(EnvironmentDecl decl) {
				if (!(decl instanceof TypeDecl)) return false;
				TypeDecl type = (TypeDecl)decl;
				String typeName = type.getName().getSingleComponent();
				return this.simpleName.equals(typeName);
			}
		}
		
		// Make sure the package name isn't a prefix of any qualified type name on the classpath.
		if (this.packageName != null) {
			final String ourPackageName = this.packageName.toString();
			Cons<EnvironmentDecl> maybePrefix = Cons.filter(parentEnvironment,
					new Predicate<EnvironmentDecl>() {
						public boolean test(EnvironmentDecl decl) {
							if (!(decl instanceof TypeDecl)) return false;
							TypeDecl type = (TypeDecl)decl;
							if (type.getPackageName() == null) return false;  // Don't care about types in default package.
							String qualifiedName = type.getCanonicalName();
							return ourPackageName.startsWith(qualifiedName);
						}
				});
			
			if (maybePrefix != null) {
				throw new ImportException.PackagePrefix(this.packageName, (TypeDecl)maybePrefix.head);
			}
		}
		
		// Don't inherit everything from the parent environment.
		this.environment = new Cons<EnvironmentDecl>(this.typeDecl, null);
		
		for (Identifier id : imports) {
			if (!id.isStarImport()) {
				final List<String> packageName = id.getPackageName();
				final String typeName = id.getLastComponent();
				Cons<EnvironmentDecl> maybeTypeDecl = Cons.filter(parentEnvironment,
						new Predicate<EnvironmentDecl>() {
							public boolean test(EnvironmentDecl decl) {
								if (!(decl instanceof TypeDecl)) return false;
								TypeDecl type = (TypeDecl)decl;
								Identifier typePackageName = type.getPackageName();
								return (typePackageName == null ? packageName.isEmpty() : typePackageName.getComponents().equals(packageName))
									 && type.getName().getLastComponent().equals(typeName);
							}
					});
				if (maybeTypeDecl == null) {
					// Tried to import a non-existent type.
					throw new ImportException.NonExistentType(id);
				}
				if (maybeTypeDecl.tail != null) {
					// More than one type has the same canonical name.
					throw new ImportException.DuplicateType(id);
				}
				Cons<EnvironmentDecl> maybeClash = Cons.filter(this.environment,
						new Predicate<EnvironmentDecl>() {
							public boolean test(EnvironmentDecl decl) {
								if (!(decl instanceof TypeDecl)) return false;
								TypeDecl type = (TypeDecl)decl;
								Identifier typePackageName = type.getPackageName();
								return !(typePackageName == null ? packageName.isEmpty() : typePackageName.getComponents().equals(packageName))
									    && type.getName().getLastComponent().equals(typeName);
							}
					});
				if (maybeClash != null) {
					// We tried to import a (different) type with the same simple name already.
					throw new ImportException.Clash(id, (TypeDecl)maybeClash.head);
				}
				
				// Importing yourself or repeating an import has no effect.
				if (!this.typeDecl.equals((TypeDecl)maybeTypeDecl.head)
				 && !Cons.contains(this.environment, maybeTypeDecl.head)) {
					// If everything is OK, add the imported type to our environment.
					this.environment = new Cons<EnvironmentDecl>(maybeTypeDecl.head, this.environment);
				}
			}
		}
		// Don't care about clashes anymore.
		
		// First, process the implicit current-package star-import...
		{
			List<String> currentPackage = 
				this.packageName == null ? new ArrayList<String>() : this.packageName.components;
			Cons<EnvironmentDecl> maybeTypeDecls =
				Cons.filter(parentEnvironment, new MatchesPackage(currentPackage));	
			for (EnvironmentDecl decl : Cons.toList(maybeTypeDecls)) {
				// decl's qualified name must not already be taken.
				// Checking that decl's simple name is not equal to the current file's is sufficient.
				assert(decl instanceof TypeDecl);

				if (decl.getName().equals(this.typeDecl.getName()) && decl != this.typeDecl) {
					throw new ImportException.DuplicateTypeDefinition(this.typeDecl, (TypeDecl)decl);
				}
				
				// Only import decl if its simple name is not taken.
				Cons<EnvironmentDecl> declsMatchingSimpleName =
						Cons.filter(this.environment, new MatchesSimpleName(decl.getName().getSingleComponent()));
				if (declsMatchingSimpleName == null && !Cons.contains(this.environment, decl)) {
					this.environment = new Cons<EnvironmentDecl>(decl, this.environment);
				}
			}
		}
		
		Cons<EnvironmentDecl> explicitOrSamePackage = this.environment;
		
		// ...then process the actual star imports...
		for (Identifier id : imports) {
			if (id.isStarImport()) {
				Cons<EnvironmentDecl> maybeTypeDecls =
					Cons.filter(parentEnvironment, new MatchesPackageOrPrefix(id.getPackageName()));
				if (maybeTypeDecls == null) {
					// Non-existent package.
					throw new ImportException.NonExistentPackage(id);
				}
				
				for (EnvironmentDecl decl : Cons.toList(maybeTypeDecls)) {
					Cons<EnvironmentDecl> wouldHide =
							Cons.filter(explicitOrSamePackage, new MatchesSimpleName(decl.getName().getLastComponent()));
					if (!Cons.contains(this.environment, decl) && wouldHide == null) {
						this.environment = new Cons<EnvironmentDecl>(decl, this.environment);
					}
				}
			}
		}
		
		// ...then add everything in java.lang that wasn't already added
		{
		List<String> javaDotLang = new ArrayList<String>(Arrays.asList("java", "lang"));
			Cons<EnvironmentDecl> javaLangDecls =
				Cons.filter(parentEnvironment, new MatchesPackage(javaDotLang));
			for (EnvironmentDecl decl : Cons.toList(javaLangDecls)) {
				Cons<EnvironmentDecl> wouldHide =
						Cons.filter(explicitOrSamePackage, new MatchesSimpleName(decl.getName().getLastComponent()));
				if (!Cons.contains(this.environment, decl) && wouldHide == null) {
					this.environment = new Cons<EnvironmentDecl>(decl, this.environment);
				}
			}
		}
		
		typeDecl.buildEnvironment(this.environment);
	}
	
	public EnvironmentDecl exportEnvironmentDecls() {
		return this.typeDecl.exportEnvironmentDecls();
	}

	@Override public void linkTypes(Cons<TypeDecl> allTypes) throws TypeLinkingException {
		typeDecl.linkTypes(allTypes);
	}
	
	@Override public void linkNames(TypeDecl curType, boolean staticCtx, EnvironmentDecl curDecl, Local curLocal, boolean lValue) throws NameLinkingException {
		typeDecl.linkNames(curType, staticCtx, curDecl, curLocal, false);
	}

	@Override public void checkTypes() throws TypeCheckingException {
		this.typeDecl.checkTypes();
	}
	
	@Override public void checkReachability(boolean canLeavePrevious) throws ReachabilityException {
		this.typeDecl.checkReachability(true);
	}
	
	// ---------- Code generation ----------

	@Override public void generateCode(AsmWriter writer, Frame frame) {
		
		writer.pushComment("Source file: %s", parseTree.getToken().getFileName());
		// Special code for java.lang.String
		if (this.typeDecl == Program.javaLangString) {
			Program.generateStringTable(writer);
		}
		this.typeDecl.generateCode(writer, frame);
		writer.popComment();
		
	}
}
