package AbstractSyntax;

import Exceptions.ImportException;
import Exceptions.NameConflictException;
import Exceptions.NameLinkingException;
import Exceptions.ReachabilityException;
import Exceptions.TypeCheckingException;
import Exceptions.TypeLinkingException;
import Parser.ParseTree;
import Types.Type;
import Utilities.Cons;
import Utilities.Predicate;

public class Local extends BlockStatement
		implements EnvironmentDecl, Identifier.Interpretation {
	
	protected Identifier typeName;
	protected Type type;
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

	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException, ImportException {
		// Make sure our name is not already taken.
		checkNameConflicts(parentEnvironment);

		this.environment = parentEnvironment;
		this.typeName.buildEnvironment(this.environment);
		this.name.buildEnvironment(this.environment);
		this.initializer.buildEnvironment(new Cons<EnvironmentDecl>(this, this.environment));
	}

	public EnvironmentDecl exportEnvironmentDecls() {
		return this;
	}

	@Override
	public void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException {
		this.type = this.typeName.resolveType(types, this.environment);
		this.initializer.linkTypes(types);
	}
	
	@Override
	public void linkNames(TypeDecl curType, boolean staticCtx, EnvironmentDecl curDecl, Local curLocal, boolean lValue) throws NameLinkingException {
		this.initializer.linkNames(curType, staticCtx, curDecl, this, false);
	}

	@Override
	public void checkTypes() throws TypeCheckingException {
		this.initializer.checkTypes();
		// Initializer must be assignable to the variable type.
		if (!this.initializer.getType().canBeAssignedTo(this.type)) {
			throw new TypeCheckingException.TypeMismatch(this.initializer, this.type.getCanonicalName());
		}
	}
	
	@Override public void checkReachability(boolean canLeavePrevious) throws ReachabilityException {
		// A local variable declaration can complete normally iff it is reachable.
		this.canEnter = canLeavePrevious;
		if (!this.canEnter) {
			throw new ReachabilityException.UnreachableStatement(this);
		}
		this.canLeave = this.canEnter;
	}

}
