package AbstractSyntax;

import java.util.ArrayList;
import java.util.List;

import Parser.ParseTree;
import Utilities.Cons;

public class Constructor extends ASTNode implements EnvironmentDecl {
	// The class this constructor belongs to.
	protected TypeDecl parent;
	protected Identifier name;
	protected List<Modifier> modifiers;
	protected List<Formal> parameters;
	protected Block block;
	
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
	}
	
	private void extractConstructorDeclarator(ParseTree tree) {
		assert(tree.getSymbol().equals("ConstructorDeclarator"));

		this.name = new Identifier(tree.getChildren()[0]);
		parameters = new ArrayList<Formal>();
		if (tree.numChildren() == 3) {
			// Do nothing
		} else if (tree.numChildren() == 4) {
			Method.extractFormalParameterList(tree.getChildren()[2]);
		}
	}

	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException {
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
}
