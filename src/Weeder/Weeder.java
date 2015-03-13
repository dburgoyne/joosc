package Weeder;

import java.util.ArrayList;
import java.util.HashSet;

import Parser.ParseException;
import Parser.ParseTree;
import Parser.ParseTree.Visitor;
import Scanner.Token;
import Scanner.TokenType;
import Utilities.StringUtils;

public abstract class Weeder implements Visitor{
	public void visit(Token token) throws ParseException{
	}
	
	public void visit(String lhs,ParseTree... children) throws ParseException{
		for(ParseTree tree:children){
			tree.visit(this);
		}
	}
	
	public static void weed(ParseTree parseTree) throws ParseException {
		parseTree.visit(new CompilationUnitWeeder());
		parseTree.visit(new CastWeeder());
		parseTree.visit(new IntegerLiteralWeeder());
	}
}

// Works for the root CompilationUnit non-terminal.
class CompilationUnitWeeder extends Weeder{
	public void visit(String lhs,ParseTree... children) throws ParseException{
		for(ParseTree tree:children){
			if(tree.getSymbol().equals("ClassDeclaration")){
				tree.visit(new ClassWeeder());
			}else if(tree.getSymbol().equals("InterfaceDeclaration")){
				tree.visit(new InterfaceWeeder());
			}else{
				tree.visit(this);
			}
		}
	}
}

// Works for class declaration
class ClassWeeder extends Weeder{
	public void visit(String lhs,ParseTree... children) throws ParseException{
		assert(lhs.equals("ClassDeclaration"));
		ListWeeder lw=new ListWeeder();
		children[0].visit(lw);
		// A class cannot be both abstract and final
		assert children[2].isTerminal();
		Token token=children[2].getToken();
		if(lw.list.contains("abstract")&&lw.list.contains("final")){
			String error = String.format(
					"Error: Class %s is both abstract and final.\n"
				  + "Line: %d Column: %d",
				    token.getLexeme(),
				    token.getLine(),
				    token.getColumn());
			throw new ParseException(error);
		}
		children[2].visit(new NameWeeder());
		children[children.length-1].visit(new ClassBodyWeeder(token));
	}
}

//Works for interface declaration
class InterfaceWeeder extends Weeder{
	public void visit(String lhs,ParseTree... children) throws ParseException{
		assert(lhs.equals("InterfaceDeclaration"));
		ListWeeder lw = new ListWeeder();
		children[0].visit(lw);
		LeftmostTokenExtractor lte = new LeftmostTokenExtractor();
		children[2].visit(lte);
		
		// Cannot have repeated modifiers.
		HashSet<String> set = new HashSet<String>();
		set.addAll(lw.list);
		if (set.size() < lw.list.size()) {
			String error = String.format(
					"Error: Repeated modifiers are not allowed.\n"
				  + "Line: %d Column: %d",
				    lte.token.getLine(),
				    lte.token.getColumn());
			throw new ParseException(error);
		}
		
		if(lw.list.contains("final")){
			String error = String.format(
					"Error: Interrface %s cannot be final.\n"
				  + "Line: %d Column: %d",
				    lte.token.getLexeme(),
				    lte.token.getLine(),
				    lte.token.getColumn());
			throw new ParseException(error);
		}
		
		children[2].visit(new NameWeeder());
		children[children.length-1].visit(new InterfaceBodyWeeder(children[1].getToken()));
	}
}

// ------------------------------------------------------------
// Weeders below will not be called directly by basic weeder
// ------------------------------------------------------------

// Works for class body
class ClassBodyWeeder extends Weeder{
	Token token;
	
	public ClassBodyWeeder(Token token){
		this.token=token;
	}
	
	public void visit(String lhs,ParseTree... children) throws ParseException{
		assert(lhs.equals("ClassBody"));
		ClassBodyDeclarationsWeeder cbds = new ClassBodyDeclarationsWeeder(token);
		children[1].visit(cbds);
		
		if (!cbds.hasConstructor) {
			String error = String.format(
                    "Error: Class %s has no constructor.\n"
                  + "Line: %d Column: %d",
                    token.getLexeme(),
                    token.getLine(),
                    token.getColumn());
            throw new ParseException(error);
		}
	}
}

// Works for class member declarations
class ClassBodyDeclarationsWeeder extends ClassBodyWeeder{
	
	public boolean hasConstructor = false;
	
	public ClassBodyDeclarationsWeeder(Token token){
		super(token);
	}
	
	public void visit(String lhs,ParseTree... children) throws ParseException{
		assert(lhs.equals("ClassBodyDeclarations"));
		
		ClassBodyDeclarationWeeder  cbd  = new ClassBodyDeclarationWeeder(token);
		ClassBodyDeclarationsWeeder cbds = new ClassBodyDeclarationsWeeder(token);
		if(children.length==1){
			children[0].visit(cbd);
		}else{
			children[0].visit(cbds);
			children[1].visit(cbd);
		}
		hasConstructor = ( cbd.hasConstructor || cbds.hasConstructor );
	}
}

// Works for class member declaration
class ClassBodyDeclarationWeeder extends ClassBodyWeeder{
	boolean hasConstructor=false;
	
	public ClassBodyDeclarationWeeder(Token token){
		super(token);
	}
	
	public void visit(String lhs,ParseTree... children) throws ParseException{
		assert(lhs.equals("ClassBodyDeclaration"));
		if(children[0].getSymbol().equals("ClassMemberDeclaration")){
			children[0].visit(new ClassMemberWeeder());
		}else{
			children[0].visit(new ConstructorWeeder());
			hasConstructor=true;
		}
	}
}

// Works for class members
class ClassMemberWeeder extends Weeder{
	public void visit(String lhs,ParseTree... children) throws ParseException{
		assert(lhs.equals("ClassMemberDeclaration"));
		if(children[0].getSymbol().equals("FieldDeclaration")){
			children[0].visit(new FieldWeeder());
		}else if(children[0].getSymbol().equals("MethodDeclaration")){
			children[0].visit(new MethodWeeder(true));
		}
	}
}

// Works for fields
class FieldWeeder extends Weeder{
	public void visit(String lhs,ParseTree... children) throws ParseException{
		assert(lhs.equals("FieldDeclaration"));
		ListWeeder lw=new ListWeeder();
		children[0].visit(lw);
		VariableDeclaratorWeeder vdw=new VariableDeclaratorWeeder();
		children[2].visit(vdw);
		
		// Cannot have repeated modifiers.
		HashSet<String> set = new HashSet<String>();
		set.addAll(lw.list);
		if (set.size() < lw.list.size()) {
			String error = String.format(
					"Error: Repeated modifiers are not allowed.\n"
				  + "Line: %d Column: %d",
				    vdw.token.getLine(),
				    vdw.token.getColumn());
			throw new ParseException(error);
		}
		
		if(lw.list.contains("final") || lw.list.contains("abstract") ){
			Token token=vdw.token;
			String error = String.format(
					"Error: Illegal modifier on field %s.\n"
				  + "Line: %d Column: %d",
				    token.getLexeme(),
				    token.getLine(),
				    token.getColumn());
			throw new ParseException(error);
		}
	}
}

// Works for variables
class VariableDeclaratorWeeder extends Weeder{
	Token token;
	
	public void visit(String lhs,ParseTree... children) throws ParseException{
		assert(lhs.equals("VariableDeclarator"));
		token=children[0].getChildren()[0].getToken();
	}
}

// Works for constructors
class ConstructorWeeder extends Weeder{
	public void visit(String lhs,ParseTree... children) throws ParseException{
		assert(lhs.equals("ConstructorDeclaration"));
		if (children.length == 3) {
			ListWeeder lw = new ListWeeder();
			children[0].visit(lw);
			LeftmostTokenExtractor lte = new LeftmostTokenExtractor();
			children[1].visit(lte);
			
			// Cannot have repeated modifiers.
			HashSet<String> set = new HashSet<String>();
			set.addAll(lw.list);
			if (set.size() < lw.list.size()) {
				String error = String.format(
						"Error: Repeated modifiers are not allowed.\n"
					  + "Line: %d Column: %d",
					    lte.token.getLine(),
					    lte.token.getColumn());
				throw new ParseException(error);
			}
			
			if (lw.list.contains("abstract")
			 || lw.list.contains("static")
			 || lw.list.contains("final")
			 || lw.list.contains("native")) {
				String error = String.format(
						"Error: Illegal constructor modifier %s.\n"
					  + "Line: %d Column: %d",
					    lte.token.getLexeme(),
					    lte.token.getLine(),
					    lte.token.getColumn());
				throw new ParseException(error);
			}
		}
	}
}

// Works for interface body
class InterfaceBodyWeeder extends Weeder{
	Token token;
	
	public InterfaceBodyWeeder(Token token){
		this.token=token;
	}
	
	public void visit(String lhs,ParseTree... children) throws ParseException{
		assert(lhs.equals("InterfaceBody"));
		if(children.length==3){
			children[1].visit(new InterfaceMemberDeclarationsWeeder(token));
		}
	}
}

// Works for interface member declarations
class InterfaceMemberDeclarationsWeeder extends InterfaceBodyWeeder{
	Token token;
	
	public InterfaceMemberDeclarationsWeeder(Token token){
		super(token);
	}
	
	public void visit(String lhs,ParseTree... children) throws ParseException{
		assert(lhs.equals("InterfaceMemberDeclarations"));
		if(children.length==1){
			children[0].visit(new InterfaceMemberDeclarationWeeder(token));
		}else{
			children[0].visit(this);
			children[1].visit(new InterfaceMemberDeclarationWeeder(token));
		}
	}
}

// Works for interface member declaration
class InterfaceMemberDeclarationWeeder extends InterfaceBodyWeeder{
	Token token;
	
	public InterfaceMemberDeclarationWeeder(Token token){
		super(token);
	}
	
	public void visit(String lhs,ParseTree... children) throws ParseException{
		assert(lhs.equals("InterfaceMemberDeclaration"));
		if(children[0].getSymbol().equals("AbstractMethodDeclaration")){
			children[0].visit(new AbstractMethodWeeder());
		}
	}
}

// Works for abstract methods
class AbstractMethodWeeder extends Weeder{
	public void visit(String lhs,ParseTree... children) throws ParseException{
		assert(lhs.equals("AbstractMethodDeclaration"));
		MethodDeclaratorWeeder mdw=new MethodDeclaratorWeeder(false);
		if(children.length==4){
			ListWeeder lw=new ListWeeder();
			children[0].visit(lw);
			children[2].visit(mdw);
			
			// Cannot have repeated modifiers.
			HashSet<String> set = new HashSet<String>();
			set.addAll(lw.list);
			if (set.size() < lw.list.size()) {
				String error = String.format(
						"Error: Repeated modifiers are not allowed.\n"
					  + "Line: %d Column: %d",
					    mdw.token.getLine(),
					    mdw.token.getColumn());
				throw new ParseException(error);
			}
			
			// An interface method cannot be static, final, or native
			if(lw.list.contains("static")
			|| lw.list.contains("final")
			|| lw.list.contains("native")){
				String error = String.format(
						"Error: Interface method %s cannot be static, final, or native.\n"
					  + "Line: %d Column: %d",
					    mdw.token.getLexeme(),
					    mdw.token.getLine(),
					    mdw.token.getColumn());
				throw new ParseException(error);
			}
		}
	} 
}

// Works for methods
class MethodWeeder extends Weeder{
	boolean canHaveBody;
	
	public MethodWeeder(boolean canHaveBody){
		this.canHaveBody=canHaveBody;
	}
	
	public void visit(String lhs,ParseTree... children) throws ParseException{
		assert(lhs.equals("MethodDeclaration"));
		MethodHeaderWeeder mhw=new MethodHeaderWeeder(canHaveBody);
		children[0].visit(mhw);
		canHaveBody=mhw.canHaveBody;
		MethodBodyWeeder mbw=new MethodBodyWeeder(mhw.token,canHaveBody);
		children[1].visit(mbw);
	} 
}

// Works for method header
class MethodHeaderWeeder extends MethodWeeder{
	Token token;
	
	public MethodHeaderWeeder(boolean canHaveBody){
		super(canHaveBody);
	}
	
	public void visit(String lhs,ParseTree... children) throws ParseException{
		assert(lhs.equals("MethodHeader"));
		ListWeeder lw=new ListWeeder();
		children[0].visit(lw);
		MethodDeclaratorWeeder mdw=new MethodDeclaratorWeeder(canHaveBody);
		children[2].visit(mdw);
		token=mdw.token;
		
		// Cannot have repeated modifiers.
		HashSet<String> set = new HashSet<String>();
		set.addAll(lw.list);
		if (set.size() < lw.list.size()) {
			String error = String.format(
					"Error: Repeated modifiers are not allowed.\n"
				  + "Line: %d Column: %d",
				    token.getLine(),
				    token.getColumn());
			throw new ParseException(error);
		}
		
		if(lw.list.contains("abstract")){
			canHaveBody=false;
			// An abstract method cannot be static or final
			if(lw.list.contains("static")||lw.list.contains("final")){
				String error = String.format(
						"Error: Abstract method %s cannot be static or final.\n"
					  + "Line: %d Column: %d",
					    token.getLexeme(),
					    token.getLine(),
					    token.getColumn());
				throw new ParseException(error);
			}
		}
		if(lw.list.contains("native")){
			canHaveBody=false;
			// A native method must be static
			if(!lw.list.contains("static")){
				String error = String.format(
						"Error: Native method %s must be static.\n"
					  + "Line: %d Column: %d",
					    token.getLexeme(),
					    token.getLine(),
					    token.getColumn());
				throw new ParseException(error);
			}
		}
		// A static method cannot be final
		if(lw.list.contains("static")&&lw.list.contains("final")){
			String error = String.format(
					"Error: Static method %s cannot be final.\n"
				  + "Line: %d Column: %d",
				    token.getLexeme(),
				    token.getLine(),
				    token.getColumn());
			throw new ParseException(error);
		}
		// Methods cannot be package private
		if(!lw.list.contains("public")&&!lw.list.contains("protected")){
			String error = String.format(
					"Error: Method %s cannot be package private.\n"
				  + "Line: %d Column: %d",
				    token.getLexeme(),
				    token.getLine(),
				    token.getColumn());
			throw new ParseException(error);
		}
	} 
}

// Works for method declarator
class MethodDeclaratorWeeder extends MethodHeaderWeeder{
	public MethodDeclaratorWeeder(boolean canHaveBody){
		super(canHaveBody);
	}

	public void visit(String lhs,ParseTree... children) throws ParseException{
		assert(lhs.equals("MethodDeclarator"));
		token=children[0].getToken();
	} 
}

// Works for method body
class MethodBodyWeeder extends MethodWeeder{
	Token token;
	
	public MethodBodyWeeder(Token token,boolean canHaveBody){
		super(canHaveBody);
		this.token=token;
	}

	public void visit(String lhs,ParseTree... children) throws ParseException{
		assert(lhs.equals("MethodBody"));
		assert(children.length == 1);
		boolean hasBody = children[0].getSymbol().equals("Block");
		if(!canHaveBody && hasBody){
			String error = String.format(
					"Error: Method %s cannot have a body.\n"
				  + "Line: %d Column: %d",
				    token.getLexeme(),
				    token.getLine(),
				    token.getColumn());
			throw new ParseException(error);
		}
	} 
}

// Works for modifiers and ...
class ListWeeder extends Weeder{
	public ArrayList<String> list;
	
	public ListWeeder(){
		list=new ArrayList<String>();
	}
	
	public void visit(Token token){
		list.add(token.getCfgName());
	}
	
	public void visit(String lhs,ParseTree... children) throws ParseException{
		for(ParseTree tree:children){
			tree.visit(this);
		}
	}
}

// Works for class and interface names
class NameWeeder extends Weeder{
	final String SOURCE_EXTENSION = ".java";
	public void visit(Token token) throws ParseException{
		if(!StringUtils.extractFileName(token.getFileName()).equals(token.getLexeme() + SOURCE_EXTENSION)){
			String error = String.format(
					"Error: Class/interface %s must be declared in a .java file with the same base name as the class/interface.\n"
				  + "Line: %d Column: %d",
				    token.getLexeme(),
				    token.getLine(),
				    token.getColumn());
			throw new ParseException(error);
		}
	}
}

// Traverses whole tree to validate casts.
class CastWeeder extends Weeder{
	public void visit(String lhs,ParseTree... children) throws ParseException{
		if (lhs.equals("CastExpression")) {
			assert(children.length == 4);
			if (children[1].getSymbol().equals("Expression")) {
				ExpressionDerivesAmbiguousNameEnforcer enforcer
					= new ExpressionDerivesAmbiguousNameEnforcer();
				children[1].visit(enforcer);
				if (!enforcer.isAmbiguousName) {
					String error = String.format(
							"Error: Cast expression beginning with %s must name a valid type.\n"
						  + "Line: %d Column: %d",
						    enforcer.token.getLexeme(),
						    enforcer.token.getLine(),
						    enforcer.token.getColumn());
					throw new ParseException(error);
				}
			}
		} else {
			for(ParseTree child : children) {
				child.visit(this);
			}
		}
	}
}

// Weeds out bad integer literals.
class IntegerLiteralWeeder extends Weeder{
	public void visit(String lhs,ParseTree... children) throws ParseException{
		if (lhs.equals("UnaryExpression")) {
			
			boolean isNegative = false;
			LeftmostTokenExtractor extractor
				= new LeftmostTokenExtractor();
			
			if (children.length == 1) {
				children[0].visit(extractor);
			} else {
				isNegative = true;
				children[1].visit(extractor);
			}
			
			if (extractor.token.getTokenType() == TokenType.IntegerLiteral) {
				if(!StringUtils.validateIntegerLiteral(extractor.token.getLexeme(), isNegative)) {
					String error = String.format(
							"Error: Class/interface %s must be declared in a .java file with the same base name as the class/interface.\n"
						  + "Line: %d Column: %d",
						    extractor.token.getLexeme(),
						    extractor.token.getLine(),
						    extractor.token.getColumn());
					throw new ParseException(error);
				}
			} else if (children.length == 1) {
				children[0].visit(this);
			} else {
				children[1].visit(this);
			}
		} else {
			for(ParseTree child : children) {
				child.visit(this);
			}
		}
	}
}

// Enforces that an expression is really an AmbiguousName.
class ExpressionDerivesAmbiguousNameEnforcer extends Weeder{
	
	public boolean isAmbiguousName = false;
	public boolean failed = false;
	public Token token;
	
	public void visit(Token token) {
		this.token = token;
	}
	
	public void visit(String lhs,ParseTree... children) throws ParseException{
		if (lhs.equals("AmbiguousName")) {
			if (!failed) {
				isAmbiguousName = true;
			}
			children[0].visit(this);
		}
		else if (children.length == 1) {
			children[0].visit(this);
		}
		else if (isAmbiguousName && children.length != 1) {
			children[0].visit(this);
		} else {
			failed = true;
			children[0].visit(this);
		}
	}
}

// Walks down the left spine of a parse tree and extracts the token there.
class LeftmostTokenExtractor extends Weeder{
	
	public Token token;
	
	public void visit(Token token) {
		this.token = token;
	}
	
	public void visit(String lhs,ParseTree... children) throws ParseException{
		children[0].visit(this);
	}
}
