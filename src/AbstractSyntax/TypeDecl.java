package AbstractSyntax;

import java.util.List;

public class TypeDecl extends ASTNode {
	
	enum Kind {
		CLASS,
		INTERFACE
	};

	protected TypeDecl.Kind kind;
	protected String name;
	
	protected List<Modifier> modifiers;
	protected TypeDecl superclass;
	protected List<TypeDecl> interfaces;
	
	protected List<Constructor> constructors;
	protected List<Field> fields;
	protected List<Method> methods;
	
}
