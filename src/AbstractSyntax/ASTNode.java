package AbstractSyntax;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

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
	
	protected static List<String> scope;
	protected String commentName;
	
	public void generateCode(PrintWriter writer) throws IOException {
		writer.println("Code generation for " + this.getClass().getName() + " is not yet implemented.");
	}

	protected String scopeIdentifier(String name, List<Formal> parameters) {
		String identifier = scope.get(0);
		for (int i = 1; i < scope.size(); i++) {
			identifier = identifier + "." + scope.get(i);
		}
		identifier = identifier + "." + name;
		for (Formal formal : parameters) {
			identifier = identifier + "#" + formal.type.getCanonicalName();
		}
		identifier = identifier + "#";
		return identifier;
	}
	
	protected String scopeIdentifier(String name) {
		String identifier = scope.get(0);
		for (int i = 1; i < scope.size(); i++) {
			identifier = identifier + "." + scope.get(i);
		}
		identifier = identifier + "." + name;
		return identifier;
	}
	
	protected void setLabel(PrintWriter writer, String identifier) {
		writer.println(identifier + ":");
	}
	
	protected void generateComment(PrintWriter writer, boolean upper) {
		String info = String.format("; %s of code for %s.", upper ? "<- Start" : "-> End", commentName);
		writer.println(info);
	}
}
