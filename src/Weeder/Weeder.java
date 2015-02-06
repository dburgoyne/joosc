package Weeder;

import java.util.ArrayList;

import Parser.ParseTree;
import Parser.ParseTree.Visitor;
import Scanner.Token;

public class Weeder implements Visitor{
	public void visit(Token token){
	}
	
	public void visit(String lhs,ParseTree... children){
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
	public void visit(String lhs,ParseTree... children){
		assert(lhs.equals("ClassDeclaration"));
		ListWeeder lw=new ListWeeder();
		children[0].visit(lw);
		// A class cannot be both abstract and final
		if(lw.list.contains("abstract")&&lw.list.contains("final")){
			assert children[1].isTerminal();
			Token token=children[1].getToken();
			System.err.println("Error: Class "+token.getCfgName()+" is both abstract and final.");
			System.err.println("Line: "+token.getLine()+" Column: "+token.getColumn());
		}
		children[1].visit(new NameWeeder());
		children[children.length-1].visit(new Weeder());
	}
}

//Works for interface declaration
class InterfaceWeeder extends Weeder{
	public void visit(String lhs,ParseTree... children){
		assert(lhs.equals("InterfaceDeclaration"));
		children[1].visit(new NameWeeder());
		children[children.length-1].visit(new Weeder());
	}
}

// ------------------------------------------------------------
// Weeders below will not be called directly by basic weeder
// ------------------------------------------------------------

// Works for methods
class MethodWeeder extends Weeder{
	boolean canHasBody;
	
	public MethodWeeder(boolean canHasBody){
		this.canHasBody=canHasBody;
	}
	
	public void visit(String lhs,ParseTree... children){
		assert(lhs.equals("MethodDeclaration"));
		MethodHeaderWeeder mhw=new MethodHeaderWeeder(canHasBody);
		children[0].visit(mhw);
		canHasBody=mhw.canHasBody;
		MethodBodyWeeder mbw=new MethodBodyWeeder(mhw.token,canHasBody);
		children[1].visit(mbw);
	} 
}

// Works for method header
class MethodHeaderWeeder extends MethodWeeder{
	Token token;
	
	public MethodHeaderWeeder(boolean canHasBody){
		super(canHasBody);
	}
	
	public void visit(String lhs,ParseTree... children){
		assert(lhs.equals("MethodHeader"));
		ListWeeder lw=new ListWeeder();
		children[0].visit(lw);
		MethodDeclaratorWeeder mdw=new MethodDeclaratorWeeder(canHasBody);
		children[2].visit(mdw);
		token=mdw.token;
		if(lw.list.contains("abstract")){
			canHasBody=false;
			// An abstract method cannot be static or final
			if(lw.list.contains("static")||lw.list.contains("final")){
				System.err.println("Error: Abstract method "+token.getCfgName()+" cannot be static or final.");
				System.err.println("Line: "+token.getLine()+" Column: "+token.getColumn());
			}
		}
		if(lw.list.contains("native")){
			canHasBody=false;
			// A native method must be static
			if(!lw.list.contains("static")){
				System.err.println("Error: Native method "+token.getCfgName()+" must be static.");
				System.err.println("Line: "+token.getLine()+" Column: "+token.getColumn());
			}
		}
		// A static method cannot be final
		if(lw.list.contains("static")&&lw.list.contains("final")){
			System.err.println("Error: Static method "+token.getCfgName()+" cannot be final.");
			System.err.println("Line: "+token.getLine()+" Column: "+token.getColumn());
		}
	} 
}

// Works for method declarator
class MethodDeclaratorWeeder extends MethodHeaderWeeder{
	public MethodDeclaratorWeeder(boolean canHasBody){
		super(canHasBody);
	}

	public void visit(String lhs,ParseTree... children){
		assert(lhs.equals("MethodDeclarator"));
		token=children[0].getToken();
	} 
}

// Works for method body
class MethodBodyWeeder extends MethodWeeder{
	Token token;
	
	public MethodBodyWeeder(Token token,boolean canHasBody){
		super(canHasBody);
		this.token=token;
	}

	public void visit(String lhs,ParseTree... children){
		assert(lhs.equals("MethodBody"));
		if(!canHasBody&&children.length==2){
			System.err.println("Error: Method "+token.getCfgName()+" cannot have body.");
			System.err.println("Line: "+token.getLine()+" Column: "+token.getColumn());
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
	
	public void visit(String lhs,ParseTree... children){
		for(ParseTree tree:children){
			tree.visit(this);
		}
	}
}

// Works for class and interface names
class NameWeeder extends Weeder{
	public void visit(Token token){
		String[] file=token.getFileName().split(".");
		if(file.length!=2||!token.getCfgName().equals(file[0])||!file[1].equals("java")){
			System.err.println("Error: Class/interface "+token.getCfgName()+" must be declared in a .java file with the same base name as the class/interface.");
			System.err.println("Line: "+token.getLine()+" Column: "+token.getColumn());
		}
	}
}
