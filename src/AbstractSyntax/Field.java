package AbstractSyntax;

import java.util.List;

import Parser.ParseTree;
import Utilities.Cons;
import Utilities.Predicate;

public class Field extends Decl {

	protected List<Modifier> modifiers;
	protected Expression initializer;
	
	protected Identifier typeName;
	protected Type type;
	
	public Identifier getName() {
		return this.name;
	}
	
	public Field(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("FieldDeclaration"));
		
		this.modifiers = Modifier.extractModifiers(tree.getChildren()[0]);
		this.typeName = new Identifier(tree.getChildren()[1]);
		extractVariableDeclarator(tree.getChildren()[2]);
	}
	
	private void extractVariableDeclarator(ParseTree tree) {
		assert(tree.getSymbol().equals("VariableDeclarator"));
		extractVariableDeclaratorId(tree.getChildren()[0]);
		if (tree.numChildren() == 3) {
			extractVariableInitializer(tree.getChildren()[2]);
		}
	}

	private void extractVariableDeclaratorId(ParseTree tree) {
		assert(tree.getSymbol().equals("VariableDeclaratorId"));
		this.name = new Identifier(tree.getChildren()[0]);
	}
	
	private void extractVariableInitializer(ParseTree tree) {
		assert(tree.getSymbol().equals("VariableInitializer"));
		this.initializer = Expression.extractExpression(tree.getChildren()[0]);
	}
	
	protected void checkNameConflicts(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException {
		final Identifier name = this.getName();
		Cons<EnvironmentDecl> conflicts = Cons.filter(parentEnvironment,
				new Predicate<EnvironmentDecl>() {
					public boolean test(EnvironmentDecl decl) {
						if (!(decl instanceof Field)) return false;
						Field field = (Field)decl;
						return field.getName().equals(name);
					}
			});
		if(conflicts != null) {
			// Give up.
			throw new NameConflictException((Field)conflicts.head, this);
		}
	}

	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException, ImportException {
		// Make sure our name is not already taken.
		checkNameConflicts(parentEnvironment);

		this.typeName.buildEnvironment(this.environment);
		this.environment = parentEnvironment;
		if (this.initializer != null) {
			this.initializer.buildEnvironment(this.environment);
		}
	}

	public EnvironmentDecl exportEnvironmentDecls() {
		return this;
	}

	@Override
	public void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException {
		this.type = this.typeName.resolveType(types, this.environment);
		this.initializer.linkTypes(types);
	}
}
