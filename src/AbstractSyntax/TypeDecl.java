package AbstractSyntax;

import java.util.ArrayList;
import java.util.List;

import Parser.ParseTree;
import Utilities.Cons;

public class TypeDecl extends ASTNode implements EnvironmentDecl {
	
	enum Kind {
		CLASS,
		INTERFACE
	};
	
	protected List<Modifier> modifiers;
	protected TypeDecl.Kind kind;
	protected Identifier name;
	
	// TODO Resolve these during environment building
	protected TypeDecl superclass;
	protected List<TypeDecl> interfaces;
	protected Identifier superclassName;
	protected List<Identifier> interfacesNames;
	
	protected List<Constructor> constructors;
	protected List<Field> fields;
	protected List<Method> methods;
	
	public Identifier getName() {
		return this.name;
	}
	
	public TypeDecl(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("TypeDeclaration"));
		
		this.interfaces = new ArrayList<TypeDecl>();
		this.interfacesNames = new ArrayList<Identifier>();
        this.constructors = new ArrayList<Constructor>();
        this.fields = new ArrayList<Field>();
        this.methods = new ArrayList<Method>();
		
		String firstChildName = tree.getChildren()[0].getSymbol();
		if (firstChildName.equals("ClassDeclaration")) {
			tree = tree.getChildren()[0];
			this.kind = Kind.CLASS;
			this.modifiers = Modifier.extractModifiers(tree.getChildren()[0]);
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
			tree = tree.getChildren()[0];
			this.kind = Kind.INTERFACE;
			this.modifiers = Modifier.extractModifiers(tree.getChildren()[0]);
			this.name = new Identifier(tree.getChildren()[2]);
			
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
		
		if (tree.numChildren() == 3) {
			tree = tree.getChildren()[1];
			while (tree.getChildren().length == 2) {
				extractInterfaceMemberDecl(tree.getChildren()[1]);
				tree = tree.getChildren()[0];
			}
			extractInterfaceMemberDecl(tree.getChildren()[0]);
		}
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
		this.superclassName = new Identifier(tree.getChildren()[1]);
	}
	
	// Extracts implemented interface names from an Interfaces node.
	private void extractInterfaces(ParseTree tree) {
		assert(tree.getSymbol().equals("Interfaces"));
		tree = tree.getChildren()[1];
		while (tree.getChildren().length == 3) {
			Identifier identifier = new Identifier(tree.getChildren()[2]);
			this.interfacesNames.add(identifier);
			tree = tree.getChildren()[0];
		}
		Identifier identifier = new Identifier(tree.getChildren()[0]);
		this.interfacesNames.add(identifier);
	}
	
	// Extracts extended interface names from an ExtendsInterfaces node.
	private void extractExtendsInterfaces(ParseTree tree) {
		assert(tree.getSymbol().equals("ExtendsInterfaces"));
		while (tree.getChildren().length == 3) {
			Identifier identifier = new Identifier(tree.getChildren()[2]);
			this.interfacesNames.add(identifier);
			tree = tree.getChildren()[0];
		}
		Identifier identifier = new Identifier(tree.getChildren()[1]);
		this.interfacesNames.add(identifier);
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) {
		this.environment = parentEnvironment;
		
		// For each field, build its environment, then stick its exported
		// symbol in our environment (then move on to the next field).
		for (Field field : fields) {
			field.buildEnvironment(this.environment);
			List<EnvironmentDecl> exports = field.exportEnvironmentDecls();
			this.environment = this.environment.append(exports);
		}
		
		// Add all symbols exported by methods and constructors to our
		// environment before building the environments for these nodes.
		for (Constructor constructor : constructors) {
			List<EnvironmentDecl> exports = constructor.exportEnvironmentDecls();
			this.environment = this.environment.append(exports);
		}
		for (Method method : methods) {
			List<EnvironmentDecl> exports = method.exportEnvironmentDecls();
			this.environment = this.environment.append(exports);
		}
		for (Constructor constructor : constructors) {
			constructor.buildEnvironment(this.environment);
		}
		for (Method method : methods) {
			method.buildEnvironment(this.environment);
		}
		
	}
	
	public List<EnvironmentDecl> exportEnvironmentDecls() {
		List<EnvironmentDecl> exports = new ArrayList<EnvironmentDecl>();
		exports.add(this);
		return exports;
	}
}
