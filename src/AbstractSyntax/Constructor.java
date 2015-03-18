package AbstractSyntax;

import java.util.ArrayList;
import java.util.List;

import Parser.ParseTree;
import Utilities.BiPredicate;
import Utilities.Cons;
import Utilities.StringUtils;

public class Constructor extends ASTNode implements EnvironmentDecl {

	// The class this constructor belongs to.
	protected TypeDecl parent;
	protected Identifier name;
	protected List<Modifier> modifiers;
	protected List<Formal> parameters;
	protected Block block; // is not null.
	
	public Identifier getName() {
		return this.name;
	}
	
	public Constructor(ParseTree tree, TypeDecl parent) {
		super(tree);
		assert(tree.getSymbol().equals("ConstructorDeclaration"));
		this.parent = parent;
		
		if (tree.numChildren() == 2) {
			extractConstructorDeclarator(tree.getChildren()[0]);
			this.block = new Block(tree.getChildren()[1]);
		} else if (tree.numChildren() == 3) {
			this.modifiers = Modifier.extractModifiers(tree.getChildren()[0]);
			extractConstructorDeclarator(tree.getChildren()[1]);
			this.block = new Block(tree.getChildren()[2]);
		}
		
		assert this.block != null;
	}
	
	private void extractConstructorDeclarator(ParseTree tree) {
		assert(tree.getSymbol().equals("ConstructorDeclarator"));

		this.name = new Identifier(tree.getChildren()[0]);
		this.parameters = new ArrayList<Formal>();
		if (tree.numChildren() == 3) {
			// Do nothing
		} else if (tree.numChildren() == 4) {
			this.parameters = Method.extractFormalParameterList(tree.getChildren()[2]);
		}
	}

	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException, ImportException {
		this.environment = parentEnvironment;
		
		this.name.buildEnvironment(this.environment);
		// Build the environment for each formal parameter
		for (Formal formal : this.parameters) {
			formal.buildEnvironment(this.environment);
			this.environment = new Cons<EnvironmentDecl>(formal.exportEnvironmentDecls(), this.environment);
		}
		
		// Build the environment for the body statements.
		this.block.buildEnvironment(this.environment);	
	}

	public EnvironmentDecl exportEnvironmentDecls() {
		return this;
	}

	@Override public void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException {
		for (Formal formal : parameters) {
			formal.linkTypes(types);
		}
		block.linkTypes(types);
	}

	@Override public void linkNames(TypeDecl curType, boolean staticCtx, EnvironmentDecl curDecl, Local curLocal, boolean lValue) throws NameLinkingException {
		for (Formal formal : parameters) {
			formal.linkNames(curType, staticCtx, this, curLocal, false);
		}
		block.linkNames(curType, staticCtx, this, curLocal, false);
	}
	
	public static class SameSignaturePredicate implements BiPredicate<Constructor> {
		public boolean test(Constructor m1, Constructor m2) {
			if (m1.parameters.size() != m2.parameters.size()) return false;
			for (int i = 0; i < m1.parameters.size(); i++) {
				if (!       m1.parameters.get(i).type
				    .equals(m2.parameters.get(i).type))
					return false;
			}
			return true;
		}
	}
	
	public String toString() {
		return this.getName() + "("
				+ StringUtils.join(this.parameters, ", ")
				+ ")";
	}

	@Override public void checkTypes() throws TypeCheckingException {
		// The name of a constructor must be the same as the name of its enclosing class. 
		if (!this.parent.getName().equals(this.getName())) {
			throw new TypeCheckingException.BadCtorName(this, this.parent);
		}
		
		for (Formal param : this.parameters) {
			param.checkTypes();
		}
		this.block.checkTypes();
	}
	
	// ---------- For code generate ----------

	@Override
	protected void setCommentName() {
		this.commentName = String.format("Constructor %s", name.toString());
	}
		
	@Override
	protected void selfGenerate() {
		setLabel(this.scopeIdentifier(name.toString(), parameters));
		setRetrun();
	}
		
	@Override
	protected void hierarchyGenerate() {
		// TODO
		super.hierarchyGenerate();
	}
}
