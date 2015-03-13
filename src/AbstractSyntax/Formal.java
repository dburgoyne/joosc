package AbstractSyntax;

import Parser.ParseTree;
import Types.Type;
import Utilities.Cons;
import Utilities.Predicate;

public class Formal extends ASTNode 
		implements EnvironmentDecl, Identifier.Interpretation {
	protected Identifier name;
	protected Identifier typeName;
	protected Type type;
	
	public Identifier getName() {
		return this.name;
	}
	
	public Formal(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("FormalParameter"));
		this.typeName = new Identifier(tree.getChildren()[0]);
		extractVariableDeclaratorId(tree.getChildren()[1]);
	}
	
	private void extractVariableDeclaratorId(ParseTree tree) {
		assert(tree.getSymbol().equals("VariableDeclaratorId"));
		this.name = new Identifier(tree.getChildren()[0]);
	}
	
	protected void checkNameConflicts(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException {
		final Identifier name = this.getName();
		Cons<EnvironmentDecl> conflicts = Cons.filter(parentEnvironment,
				new Predicate<EnvironmentDecl>() {
					public boolean test(EnvironmentDecl decl) {
						if (!(decl instanceof Formal)) return false;
						Formal formal = (Formal)decl;
						return formal.getName().equals(name);
					}
			});
		if(conflicts != null) {
			// Give up.
			throw new NameConflictException((Formal)conflicts.head, this);
		}
	}

	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException {
		// Make sure our name is not already taken.
		checkNameConflicts(parentEnvironment);

		this.environment = parentEnvironment;
		this.typeName.buildEnvironment(this.environment);
	}

	public EnvironmentDecl exportEnvironmentDecls() {
		return this;
	}

	@Override
	public void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException {
		this.type = this.typeName.resolveType(types, this.environment);
	}

	@Override public void linkNames(TypeDecl curType, boolean staticCtx) { }
	
	public String toString() {
		return type + " " + name;
	}

	@Override public void checkTypes() throws TypeCheckingException {
		// Do nothing.
	}
}
