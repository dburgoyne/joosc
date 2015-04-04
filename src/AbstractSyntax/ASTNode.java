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
import Scanner.Token;
import Utilities.Cons;

public abstract class ASTNode {
	protected ParseTree parseTree;
	protected List<ASTNode> children;
	
	protected Cons<EnvironmentDecl> environment;
	
	public ASTNode(ParseTree tree) {
		parseTree = tree;
	}
	
	public String getPositionalString() {
		Token token = this.parseTree.getToken();
		String info = String.format("file %s, line %d, column %d",
                token.getFileName(),
                token.getLine(),
                token.getColumn());
		return info;
	}
	
	public abstract void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException, ImportException;
	public abstract EnvironmentDecl exportEnvironmentDecls();
	public abstract void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException;
	public abstract void linkNames(TypeDecl curType, boolean staticCtx, EnvironmentDecl curDecl, Local curLocal, boolean lValue) throws NameLinkingException;
	public abstract void checkTypes() throws TypeCheckingException;
	
	// ---------- Reachability analysis ----------
	protected boolean canEnter = true;
	protected boolean canLeave = true;
	public abstract void checkReachability(boolean canLeavePrevious) throws ReachabilityException;
	
	// ---------- Code generation ----------
	public void generateCode(AsmWriter writer, Frame frame) {
		writer.comment("Code generation for %s is not yet implemented.", this.getClass().getName());
	}
}
