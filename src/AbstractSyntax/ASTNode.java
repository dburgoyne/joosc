package AbstractSyntax;

import java.util.ArrayList;
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
	
	// ---------- For code generate ----------
	
	protected static List<String> scope;
	protected String commentName;
	
	public void codeGenerate() {
		setCommentName(); // Comment
		commentGenerate(true); // Comment
		selfGenerate(); 
		hierarchyGenerate();
		commentGenerate(false); // Comment
	}
	
	protected void setCommentName() {
		this.commentName = String.format("an unspecific %s", this.getClass().toString());
	}
	
	protected void selfGenerate() {
		String info = String.format("; --- Self generate code is not implement for %s.", commentName);
		System.out.println(info);
	}
	
	protected void hierarchyGenerate() {
		String info = String.format("; --- Hierarchy generate code is not implement for %s, subnodes will not be visited.", commentName);
		System.out.println(info);
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
		identifier = identifier + "#:";
		return identifier;
	}
	
	protected String scopeIdentifier(String name) {
		String identifier = scope.get(0);
		for (int i = 1; i < scope.size(); i++) {
			identifier = identifier + "." + scope.get(i);
		}
		identifier = identifier + "." + name + ":";
		return identifier;
	}
	
	private void commentGenerate(boolean upper) {
		String info = String.format("; - %s of code for %s.", upper ? "Start" : "End", commentName);
		System.out.println(info);
	}
}
