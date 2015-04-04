package AbstractSyntax;

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
	
	// ---------- For code generate ----------
	
	protected static List<String> s_scope;
	protected static List<String> s_field;
	protected static List<String> s_local;
	protected String commentName;
	
	public void codeGenerate() {
		setCommentName(); // Comment
		commentGenerate(true); // Comment
		selfGenerate(); 
		hierarchyGenerate();
		finishGenerate(); 
		commentGenerate(false); // Comment
	}
	
	public void pointerGenerate() {
		setPointer();
	}
	
	protected void setCommentName() {
		commentName = String.format("an unspecific %s", this.getClass().toString());
	}
	
	protected void selfGenerate() {
		String info = String.format("; -!- Self generate code is not implement for %s.", commentName);
		System.out.println(info);
	}
	
	protected void hierarchyGenerate() {
		String info = String.format("; -!- Hierarchy generate code is not implement for %s, subnodes will not be visited.", commentName);
		System.out.println(info);
	}
	
	protected void finishGenerate() {
		String info = String.format("; -!- Finish generate code is not implement for %s.", commentName);
		System.out.println(info);
	}
	
	protected String selfIdentifier() {
		return scopeIdentifier();
	}
	
	protected String scopeIdentifier(String name, List<Formal> parameters) {
		String identifier = scopePrefix();
		identifier = identifier + "." + name;
		for (Formal formal : parameters) {
			identifier = identifier + "#" + formal.type.getCanonicalName();
		}
		identifier = identifier + "#";
		return identifier;
	}
	
	protected String scopeIdentifier(String name) {
		String identifier = scopePrefix();
		identifier = identifier + "." + name;
		return identifier;
	}
	
	protected String scopeIdentifier() {
		String identifier = scopePrefix();
		return identifier;
	}
	
	protected void setLabel() {
		System.out.println(selfIdentifier() + ":");
	}
	
	protected void setPointer() {
		System.out.println("dd " + selfIdentifier());
	}
	
	protected void setBlank() {
		System.out.println("nop");
	}
	
	protected void setValue(String value) {
		// Fix later
		System.out.println(Integer.parseInt(value));
	}
	
	protected void backupRegisters() {
	}
	
	protected void recoverRegisters() {
	}
	
	protected void setReturn() {
		System.out.println("leave");
		System.out.println("ret");
	}
	
	private void commentGenerate(boolean upper) {
		if (commentName.length() > 0) {
			String info = String.format("; %s of code for %s.", upper ? "<- Start" : "-> End", commentName);
			System.out.println(info);
		}
	}
	
	private String scopePrefix() {
		String identifier = s_scope.get(0);
		for (int i = 1; i < s_scope.size(); i++) {
			identifier = identifier + "." + s_scope.get(i);
		}
		return identifier;
	}
}
