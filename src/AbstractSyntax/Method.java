package AbstractSyntax;

import java.util.ArrayList;
import java.util.List;

import Parser.ParseTree;

public class Method extends Decl {

	protected List<Modifier> modifiers;
	protected List<Formal> parameters;
	protected Identifier typeName;
	// TODO Fill this in during type resolution
	protected EnvironmentDecl type;
	
	protected Block statements;
	
	public Method(ParseTree tree, boolean isAbstract) {
		super(tree);
		assert(tree.getSymbol().equals("MethodDeclaration") || tree.getSymbol().equals("AbstractMethodDeclaration") );
		if (tree.getSymbol().equals("MethodDeclaration")) {
			extractMethodHeader(tree.getChildren()[0]);
			extractMethodBody(tree.getChildren()[0]);
		} else if (tree.getSymbol().equals("AbstractMethodDeclaration")) {
			if (tree.numChildren() == 3) {
				this.typeName = new Identifier(tree.getChildren()[0]);
				extractMethodDeclarator(tree.getChildren()[1]);
			} else if (tree.numChildren() == 4) {
				this.modifiers = Modifier.extractModifiers(tree.getChildren()[0]);
				this.typeName = new Identifier(tree.getChildren()[1]);
				extractMethodDeclarator(tree.getChildren()[2]);
			}
		}
	}
	
	private void extractMethodHeader(ParseTree tree) {
		assert(tree.getSymbol().equals("MethodHeader"));
		this.modifiers = Modifier.extractModifiers(tree.getChildren()[0]);
		this.typeName = new Identifier(tree.getChildren()[1]);
		extractMethodDeclarator(tree.getChildren()[2]);
	}
	
	private void extractMethodDeclarator(ParseTree tree) {
		assert(tree.getSymbol().equals("MethodDeclarator"));
		this.name = tree.getChildren()[0].getSymbol();
		if (tree.numChildren() == 4) {
			this.parameters = extractFormalParameterList(tree.getChildren()[2]);
		} else {
			return;
		}
	}
	
	public static List<Formal> extractFormalParameterList(ParseTree tree) {
		assert(tree.getSymbol().equals("FormalParameterList"));
		List<Formal> parameters = new ArrayList<Formal>();
		while (tree.numChildren() > 1) {
			Formal f = new Formal(tree.getChildren()[2]);
			parameters.add(f);
			tree = tree.getChildren()[0];
		}
		Formal f = new Formal(tree.getChildren()[0]);
		parameters.add(f);
		return parameters;
	}
	
	private void extractMethodBody(ParseTree tree) {
		assert(tree.getSymbol().equals("MethodBody"));
		if (tree.getChildren()[0].getSymbol().equals("Block")) {
			this.statements = new Block(tree.getChildren()[0]);
		} else {
			// Leave this.statements null to indicate no block (semicolon).
			return;
		}
	}
}
