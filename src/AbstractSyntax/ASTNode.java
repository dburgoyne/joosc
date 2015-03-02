package AbstractSyntax;

import java.util.List;

import Parser.ParseTree;
import Utilities.Cons;

public abstract class ASTNode {
	protected ParseTree parseTree;
	protected List<ASTNode> children;
	
	protected Cons<EnvironmentDecl> environment;
	
	public ASTNode(ParseTree tree) {
		parseTree = tree;
	}
	
	public abstract void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment);
	public abstract List<EnvironmentDecl> exportEnvironmentDecls();
}
