package AbstractSyntax;

import java.util.ArrayList;
import java.util.List;

import Parser.ParseTree;
import Utilities.Cons;
import Utilities.ObjectUtils;
import Utilities.Predicate;

public class TypeDecl extends ASTNode implements EnvironmentDecl, Type {
	
	enum Kind {
		CLASS,
		INTERFACE
	};
	
	protected List<Modifier> modifiers;
	protected TypeDecl.Kind kind;
	protected Identifier name;
	
	// TODO Resolve these during type linking
	protected TypeDecl superclass;
	protected List<TypeDecl> interfaces;
	protected Identifier superclassName;
	protected List<Identifier> interfacesNames;
	
	protected List<Constructor> constructors;
	protected List<Field> fields;
	protected List<Method> methods;
	
	// Back reference to the parent Classfile node.
	protected Classfile parent;
	
	public Identifier getName() {
		return this.name;
	}
	
	public String getCanonicalName() {
		return this.getPackageName().toString() + "." + this.getName().toString();
	}
	
	public Identifier getPackageName() {
		return this.parent.packageName;
	}
	
	public TypeDecl(ParseTree tree, Classfile parent) {
		super(tree);
		assert(tree.getSymbol().equals("TypeDeclaration"));
		this.parent = parent;
		
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
	
	protected void checkNameConflicts(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException {
		final Identifier packageName = this.getPackageName();
		final Identifier typeName = this.getName();
		Cons<EnvironmentDecl> conflicts = Cons.filter(parentEnvironment,
				new Predicate<EnvironmentDecl>() {
					public boolean test(EnvironmentDecl decl) {
						if (!(decl instanceof TypeDecl)) return false;
						TypeDecl type = (TypeDecl)decl;
						return ObjectUtils.equals(type.getPackageName(), packageName)
						    && ObjectUtils.equals(type.getName(), typeName);
					}
			});
		if(conflicts != null) {
			// Give up.
			throw new NameConflictException((TypeDecl)conflicts.head, this);
		}
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException, ImportException {
		// Make sure our canonical name is not already taken.
		checkNameConflicts(parentEnvironment);
		
		this.environment = new Cons<EnvironmentDecl>(this, parentEnvironment);
		
		// For each field, build its environment, then stick its exported
		// symbol in our environment (then move on to the next field).
		for (Field field : fields) {
			field.buildEnvironment(this.environment);
			EnvironmentDecl export = field.exportEnvironmentDecls();
			assert(export != null);
			this.environment = new Cons<EnvironmentDecl>(export, this.environment);
		}
		
		// Add all symbols exported by methods and constructors to our
		// environment before building the environments for these nodes.
		for (Constructor constructor : constructors) {
			EnvironmentDecl export = constructor.exportEnvironmentDecls();
			assert(export != null);
			this.environment = new Cons<EnvironmentDecl>(export, this.environment);
		}
		for (Method method : methods) {
			EnvironmentDecl export = method.exportEnvironmentDecls();
			assert(export != null);
			this.environment = new Cons<EnvironmentDecl>(export, this.environment);
		}
		for (Constructor constructor : constructors) {
			constructor.buildEnvironment(this.environment);
		}
		for (Method method : methods) {
			method.buildEnvironment(this.environment);
		}
	}
	
	public EnvironmentDecl exportEnvironmentDecls() {
		return this;
	}

	@Override public void linkTypes(Cons<TypeDecl> allTypes) throws TypeLinkingException {
		
		// Hierarchy decls:
		
		if (this.superclassName != null) {
			Type type = this.superclassName.resolveType(allTypes, this.environment);
			if (!(type instanceof TypeDecl)) {
				throw new TypeLinkingException.NotRefType(type, 
						this.superclassName.getPositionalString());
			}
			this.superclass = (TypeDecl)type;
		}
		
		for (Identifier iface : this.interfacesNames) {
			Type type = iface.resolveType(allTypes, this.environment);
			if (!(type instanceof TypeDecl)) {
				throw new TypeLinkingException.NotRefType(type, 
							iface.getPositionalString());
			}
			this.interfaces.add((TypeDecl)type);
		}
		
		// Class body decls: 
		
		for (Constructor ctor : constructors) {
			ctor.linkTypes(allTypes);
		}
		for (Field f : fields) {
			f.linkTypes(allTypes);
		}
		for (Method m : methods) {
			m.linkTypes(allTypes);
		}
		
	}
}
