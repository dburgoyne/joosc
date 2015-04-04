package AbstractSyntax;

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
import Types.Type;
import Utilities.BiPredicate;
import Utilities.Cons;
import Utilities.Predicate;

public class Field extends Decl implements Identifier.Interpretation {


	protected List<Modifier> modifiers;
	protected Expression initializer; // Can be null!!
	protected Cons<Field> followingFields;
	
	protected Identifier typeName;
	protected Type type;
	
	protected TypeDecl declaringType;
	
	public Identifier getName() {
		return this.name;
	}
	
	@Override public String toString() {
		return type + " " + this.getName();
	}
	
	public boolean isStatic() {
		return this.modifiers.contains(Modifier.STATIC);
	}
	
	public Field(ParseTree tree, TypeDecl declaringType) {
		super(tree);
		assert(tree.getSymbol().equals("FieldDeclaration"));
		
		this.declaringType = declaringType;
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
		// The environment includes this field; there should be exactly one "conflict".
		assert(conflicts != null);
		if (conflicts != null && conflicts.tail != null ) {
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
			this.initializer.buildEnvironment(new Cons<EnvironmentDecl>(this, this.environment));
		}
	}

	public EnvironmentDecl exportEnvironmentDecls() {
		return this;
	}

	@Override
	public void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException {
		this.type = this.typeName.resolveType(types, this.environment);
		if (this.initializer != null) {
			this.initializer.linkTypes(types);
		}
	}

	@Override
	public void linkNames(TypeDecl curType, boolean staticCtx, EnvironmentDecl curDecl, Local curLocal, boolean lValue) throws NameLinkingException {
		assert this.declaringType == curType;
		if (this.initializer != null) {
			this.initializer.linkNames(curType, staticCtx, this, curLocal, false);
		}
	}
	

	public final static class SameNamePredicate implements BiPredicate<Field> {
		public boolean test(Field t1, Field t2) {
			return t1.name.equals(t2.name);
		}

	}

	@Override public void checkTypes() throws TypeCheckingException {
		if (this.initializer != null) {
			this.initializer.checkTypes();
			this.initializer.assertNonVoid();
			
			if (!this.initializer.getType().canBeAssignedTo(this.type)) {
				throw new TypeCheckingException.TypeMismatch(this.initializer, this.type.getCanonicalName());
			}
		}
	}
	
	@Override public void checkReachability(boolean canLeavePrevious) throws ReachabilityException {
		if (this.initializer != null) {
			this.initializer.checkReachability(true); // Not really necessary
		}
	}
	
	// ---------- Code generation ----------
	
	// The offset of this field from the start of the object, in bytes.
	public int byteOffset;
	
	public String getStaticLabel() {
		assert(this.isStatic());
		return Utilities.Label.generateLabel("sf", this.declaringType.getCanonicalName(), this.name.getSingleComponent(), null);
	}

	@Override public void generateCode(AsmWriter writer, Frame frame) {
		if (this.isStatic()) {
			String label = this.getStaticLabel();
			writer.verbatimfn("global %s", label);
			writer.verbatimfn("%s: dd 0", label);
			writer.justDefinedGlobal(label);
		}
	}
}
