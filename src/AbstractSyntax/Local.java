package AbstractSyntax;

import Parser.ParseTree;
import Utilities.Cons;
import Utilities.Predicate;

public class Local extends BlockStatement implements EnvironmentDecl {
	
	protected Identifier typeName;
	// TODO Fill this is during type resolution.
	protected EnvironmentDecl type;
	protected Identifier name;
	protected Expression initializer;  // Never null!
	
	public Identifier getName() {
		return name;
	}
	
	public Local(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("LocalVariableDeclaration")
			|| tree.getSymbol().equals("LocalVariableDeclarationStatement"));
		
		if (tree.getSymbol().equals("LocalVariableDeclarationStatement")) {
			tree = tree.getChildren()[0];
		}
		assert(tree.getSymbol().equals("LocalVariableDeclaration"));
		
		this.typeName = new Identifier(tree.getChildren()[0]);
		ParseTree secondChild = tree.getChildren()[1];
		this.name = new Identifier(secondChild.getChildren()[0].getChildren()[0]);
		this.initializer = Expression.extractExpression(secondChild.getChildren()[2].getChildren()[0]);
		
	}
	
	protected void checkNameConflicts(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException {
		final Identifier name = this.getName();
		Cons<EnvironmentDecl> conflicts = Cons.filter(parentEnvironment,
				new Predicate<EnvironmentDecl>() {
					public boolean test(EnvironmentDecl decl) {
						// Can't shadow formal parameters or other locals.
						if (decl instanceof Local) {
							Local local = (Local)decl;
							return local.getName().equals(name);
						} else if (decl instanceof Formal) {
							Formal formal = (Formal)decl;
							return formal.getName().equals(name);
						}
						return false;
					}
			});
		if(conflicts != null) {
			// Give up.
			if (conflicts.head instanceof Local) {
				throw new NameConflictException((Local)conflicts.head, this);
			} else if (conflicts.head instanceof Formal) {
				throw new NameConflictException((Formal)conflicts.head, this);
			}
		}
	}

	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException {
		// Make sure our name is not already taken.
		checkNameConflicts(parentEnvironment);

		this.environment = parentEnvironment;
		this.typeName.buildEnvironment(this.environment);
		this.name.buildEnvironment(this.environment);
		this.initializer.buildEnvironment(this.environment);
	}

	public EnvironmentDecl exportEnvironmentDecls() {
		return this;
	}

}