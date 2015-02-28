package AbstractSyntax;

import java.util.List;

import Parser.ParseTree;

public class TypeDecl extends ASTNode implements EnvironmentDecl {
	
	enum Kind {
		CLASS,
		INTERFACE
	};
	
	protected List<Modifier> modifiers;
	protected TypeDecl.Kind kind;
	protected Identifier name;
	
	// TODO We don't have enough information to build TypeDecls here (yet)
	//protected TypeDecl superclass;
	//protected List<TypeDecl> interfaces;
	protected Identifier superclass;
	protected List<Identifier> interfaces;
	
	protected List<Constructor> constructors;
	protected List<Field> fields;
	protected List<Method> methods;
	
	public Identifier getName() {
		return this.name;
	}
	
	public TypeDecl(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("TypeDeclaration"));
		String firstChildName = tree.getChildren()[0].getSymbol();
		if (firstChildName.equals("ClassDeclaration")) {
			this.kind = Kind.CLASS;
			tree = tree.getChildren()[0];
			modifiers = Modifier.extractModifiers(tree.getChildren()[0]);
			this.name = new Identifier(tree.getChildren()[2]);
			
			switch (tree.numChildren()) {
			case 4:
				extractClassBodyDecls(tree.getChildren()[3]);
				break;
			case 5:
				if (tree.getChildren()[3].getSymbol().equals("Super")) {
					extractSuper(tree.getChildren()[3]);
				} else if (tree.getChildren()[3].getSymbol().equals("Interfaces")) {
					extractInterfaces(tree.getChildren()[3]);
				}
				extractClassBodyDecls(tree.getChildren()[4]);
				break;
			case 6:
				extractSuper(tree.getChildren()[3]);
				extractInterfaces(tree.getChildren()[4]);
				extractClassBodyDecls(tree.getChildren()[5]);
				break;
			}
			
			
		} else if (firstChildName.equals("InterfaceDeclaration")) {
			this.kind = Kind.INTERFACE;
			tree = tree.getChildren()[0];
			modifiers = Modifier.extractModifiers(tree.getChildren()[0]);
			name = new Identifier(tree.getChildren()[2]);
			
			switch (tree.numChildren()) {
			case 4:
				extractInterfaceMemberDecls(tree.getChildren()[3]);
				break;
			case 5:
				extractExtendsInterfaces(tree.getChildren()[3]);
				extractInterfaceMemberDecls(tree.getChildren()[4]);
				break;
			}
		} else {
			return;
		}
	}

	// Extracts constructors, fields and methods from a ClassBody node.
	private void extractClassBodyDecls(ParseTree tree) {
		assert(tree.getSymbol().equals("ClassBody"));
		tree = tree.getChildren()[1];
		while (tree.getChildren().length > 1) {
			extractClassBodyDecl(tree.getChildren()[1]);
			tree = tree.getChildren()[0];
		}
		extractClassBodyDecl(tree.getChildren()[0]);
	}

	
	// Extracts a constructor, field or method from a ClassBodyDeclaration node.
	private void extractClassBodyDecl(ParseTree tree) {
		assert(tree.getSymbol().equals("ClassBodyDeclaration"));
		ParseTree firstChild = tree.getChildren()[0];
		String firstChildName = firstChild.getSymbol();
		if (firstChildName.equals("ClassMemberDeclaration")) {
			ParseTree grandchild = firstChild.getChildren()[0];
			String grandchildName= grandchild.getSymbol();
			if (grandchildName.equals("FieldDeclaration")) {
				Field field = new Field(grandchild);
				this.fields.add(field);
			} else if (grandchildName.equals("MethodDeclaration")) {
				Method method = new Method(grandchild, false);
				this.methods.add(method);
			} else {
				return;
			}
		} else if (firstChildName.equals("ConstructorDeclaration")) {
			Constructor constructor = new Constructor(firstChild, this);
			this.constructors.add(constructor);
		}
	}
	
	// Extracts method declarations from an InterfaceBody node.
	private void extractInterfaceMemberDecls(ParseTree tree) {
		assert(tree.getSymbol().equals("InterfaceBody"));
		tree = tree.getChildren()[1];
		while (tree.getChildren().length == 2) {
			extractInterfaceMemberDecl(tree.getChildren()[1]);
			tree = tree.getChildren()[0];
		}
		extractInterfaceMemberDecl(tree.getChildren()[0]);
	}
	
	// Extracts a method declaration from an InterfaceMemberDeclaration node.
	private void extractInterfaceMemberDecl(ParseTree tree) {
		assert(tree.getSymbol().equals("InterfaceMemberDeclaration"));
		ParseTree firstChild = tree.getChildren()[0];
		String firstChildName = firstChild.getSymbol();
		if (firstChildName.equals("AbstractMethodDeclaration")) {
			Method method = new Method(firstChild, true);
			this.methods.add(method);
		} else {
			return;
		}
	}
	
	// Extracts extended class name from a Super node.
	private void extractSuper(ParseTree tree) {
		assert(tree.getSymbol().equals("Super"));
		this.superclass = new Identifier(tree.getChildren()[1]);
	}
	
	// Extracts implemented interface names from an Interfaces node.
	private void extractInterfaces(ParseTree tree) {
		assert(tree.getSymbol().equals("Interfaces"));
		tree = tree.getChildren()[1];
		while (tree.getChildren().length == 3) {
			Identifier identifier = new Identifier(tree.getChildren()[2]);
			this.interfaces.add(identifier);
			tree = tree.getChildren()[0];
		}
		Identifier identifier = new Identifier(tree.getChildren()[0]);
		this.interfaces.add(identifier);
	}
	
	// Extracts extended interface names from an ExtendsInterfaces node.
	private void extractExtendsInterfaces(ParseTree tree) {
		assert(tree.getSymbol().equals("ExtendsInterfaces"));
		while (tree.getChildren().length == 3) {
			Identifier identifier = new Identifier(tree.getChildren()[2]);
			this.interfaces.add(identifier);
			tree = tree.getChildren()[0];
		}
		Identifier identifier = new Identifier(tree.getChildren()[1]);
		this.interfaces.add(identifier);
	}
}
