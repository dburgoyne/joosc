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
	protected String name;
	
	// TODO We don't have enough information to build TypeDecls here (yet)
	//protected TypeDecl superclass;
	//protected List<TypeDecl> interfaces;
	protected Identifier superclass;
	protected List<Identifier> interfaces;
	
	protected List<Constructor> constructors;
	protected List<Field> fields;
	protected List<Method> methods;
	
	public String getName() {
		return this.name;
	}
	
	public TypeDecl(ParseTree tree) {
		super(tree);
		String firstChildName = tree.getChildren()[0].getSymbol();
		if (firstChildName.equals("ClassDeclaration")) {
			this.kind = Kind.CLASS;
			tree = tree.getChildren()[0];
			extractModifiers(tree.getChildren()[0]);
			this.name = tree.getChildren()[2].getSymbol();
			
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
			extractModifiers(tree.getChildren()[0]);
			name = tree.getChildren()[2].getSymbol();
			
			switch (tree.numChildren()) {
			case 4:
				extractInterfaceMemberDecls(tree.getChildren()[3]);
				break;
			case 5:
				extractInterfaces(tree.getChildren()[3]);
				extractInterfaceMemberDecls(tree.getChildren()[4]);
				break;
			}
		} else {
			return;
		}
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
	
	// Extracts constructors, fields and methods from a ClassBody node.
	private void extractClassBodyDecls(ParseTree tree) {
		tree = tree.getChildren()[1];
		while (tree.getChildren().length > 1) {
			extractClassBodyDecl(tree.getChildren()[1]);
			tree = tree.getChildren()[0];
		}
		extractClassBodyDecl(tree.getChildren()[0]);
	}

	
	// Extracts a constructor, field or method from a ClassBodyDeclaration node.
	private void extractClassBodyDecl(ParseTree tree) {
		String firstChildName = tree.getChildren()[0].getSymbol();
		if (firstChildName.equals("ClassMemberDeclaration")) {
			ParseTree grandchild = tree.getChildren()[0].getChildren()[0];
			String grandchildName= grandchild.getSymbol();
			if (grandchildName.equals("FieldDeclaration")) {
				Field field = new Field(grandchild);
				this.fields.add(field);
			} else if (grandchildName.equals("MethodDeclaration")) {
				Method method = new Method(grandchild);
				this.methods.add(method);
			} else {
				return;
			}
		} else if (firstChildName.equals("ConstructorDeclaration")) {
			Constructor constructor = new Constructor(tree);
			this.constructors.add(constructor);
		}
	}
	
	// Extracts method declarations from an InterfaceMemberDeclaration node.
	private void extractInterfaceMemberDecls(ParseTree tree) {
		if(tree.numChildren() == 3){
			tree=tree.getChildren()[1];
			while(tree.numChildren()>1) {
				Method method = new Method(tree.getChildren()[1]);
				this.methods.add(method);
				tree=tree.getChildren()[0];
			}
			Method method = new Method(tree.getChildren()[0]);
			this.methods.add(method);
		}
	}
	
	// Extracts extended class name from a Super node.
	private void extractSuper(ParseTree tree) {
		// Super extends AmbiguousName
		this.superclass = new Identifier(tree.getChildren()[1]);
	}
	
	// Extracts implemented/extended interface names from an Interfaces/ExtendsInterfaces node.
	private void extractInterfaces(ParseTree tree) {
		// Interfaces implements InterfaceTypeList
		// InterfaceTypeList AmbiguousName
		// InterfaceTypeList InterfaceTypeList , AmbiguousName
		tree = tree.getChildren()[1];
		while(tree.numChildren()>1) {
			Identifier identifer = new Identifier(tree.getChildren()[2]);
			this.interfaces.add(identifer);
			tree=tree.getChildren()[0];
		}
		Identifier identifer = new Identifier(tree.getChildren()[0]);
		this.interfaces.add(identifer);
	}
}
