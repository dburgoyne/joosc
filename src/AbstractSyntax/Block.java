package AbstractSyntax;

import java.util.ArrayList;
import java.util.List;

import Parser.ParseTree;
import Utilities.Cons;

public class Block extends Statement {
	protected List<BlockStatement> statements;
	
	public Block(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("Block") || tree.getSymbol().equals("ConstructorBody"));
		
		this.statements = new ArrayList<BlockStatement>();
		if (tree.numChildren() == 2) {
			// Do nothing
		} else if (tree.numChildren() == 3) {
			extractBlockStatements(tree.getChildren()[1]);
		}
	}
	
	private void extractBlockStatements(ParseTree tree) {
		assert(tree.getSymbol().equals("BlockStatements"));
		while (tree.numChildren() == 2) {
			BlockStatement bs = BlockStatement.extractBlockStatement(tree.getChildren()[1]);
			statements.add(0, bs);
			tree = tree.getChildren()[0];
		}
		BlockStatement bs = BlockStatement.extractBlockStatement(tree.getChildren()[0]);
		statements.add(0, bs);
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException, ImportException {
		this.environment = parentEnvironment;
		
		// Build the environment for the body statements.
		for (BlockStatement statement : this.statements) {
			statement.buildEnvironment(this.environment);
			EnvironmentDecl export = statement.exportEnvironmentDecls();
			if (export != null) {
				this.environment = new Cons<EnvironmentDecl>(export, this.environment);
			}
		}
	}

	@Override
	public void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException {
		for (BlockStatement bs : this.statements) {
			bs.linkTypes(types);
		}
	}
	
	@Override
	public void linkNames(TypeDecl curType, boolean staticCtx) throws NameLinkingException {
		for (BlockStatement bs : this.statements) {
			bs.linkNames(curType, staticCtx);
		}
	}
}
