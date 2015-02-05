package Weeder;

import java.util.ArrayList;

import Parser.ParseTree;
import Scanner.Token;
import Scanner.TokenType;

public class Weeder{
	public int weed(ParseTree tree){
		try{
			weedBranch(tree);
		}catch(WeederException we){
			we.printWeederMessage();
			return 42;
		}
		return 0;
	}
	
	private void weedBranch(ParseTree tree)throws WeederException{
		// Terminal symbol
		if(tree.isTerminal()){
			Token token=tree.getToken();
			TokenType type=token.getTokenType();
			if((type.equals(TokenType.StringLiteral)||type.equals(TokenType.Identifier)||type.equals(TokenType.CharacterLiteral))&&!validSevenBit(token.getFileName())){
				throw new WeederException(token,"Input contains an invalid 7-bit ASCII character");
			}
		}else{
			String symbol=tree.getSymbol();
			ParseTree[] children=tree.getChildren();
			// Class declaration
			if(symbol.equals("ClassDeclaration")){
				for(ParseTree child:children){
					if(child.getSymbol().equals("Modifiers")){
						ArrayList<String> modifiers=modifierList(child);
						// A class cannot be both abstract and final
						if(modifiers.contains("abstract")&&modifiers.contains("final")){
							assert children[2].isTerminal();
							throw new WeederException(children[2].getToken(),"Class is both abstract and final");
						}
					}else{
						weedBranch(child);
					}
				}
			// Method declaration
			}else if(symbol.equals("MethodHeader")){
				ArrayList<String> modifiers=null;
				boolean abs=false;
				boolean hasBody=false;
				Token idToken=null;
				for(ParseTree child:children){
					if(child.getSymbol().equals("Modifiers")){
						modifiers=modifierList(child);
						abs=modifiers.contains("abstract");
					}else if(child.getSymbol().equals("MethodDeclarator")){
						hasBody=child.getChildren().length==4;
						idToken=child.getChildren()[0].getToken();
						weedBranch(child);
					}else{
						weedBranch(child);
					}
				}
				// A method has a body if and only if it is not abstract
				// An abstract method cannot be static or final
				// A static method cannot be final
				if(abs==hasBody){
					throw new WeederException(idToken,"A Method has a body if and only if it is not abstract");
				}
				if(abs&&(modifiers.contains("static")||modifiers.contains("final"))){
					throw new WeederException(idToken,"An abstract method cannot be static or final");
				}
				if(modifiers.contains("static")&&modifiers.contains("final")){
					throw new WeederException(idToken,"A static method cannot be final");
				}
			// Otherwise
			}else{
				for(ParseTree child:children){
					weedBranch(child);
				}
			}
		}
	}
	
	// All characters in the input program must be in the range of 7-bit ASCII (0 to 127)
	private boolean validSevenBit(String symbol){
		for(int i=0;i<symbol.length();i++){
			if((((int)symbol.charAt(i))|127)!=127){
				return false;
			}
		}
		return true;
	}
	
	// Grab all modifiers
	private ArrayList<String> modifierList(ParseTree tree){
		ParseTree[] children=tree.getChildren();
		if(children[0].getSymbol().equals("Modifiers")){
			ArrayList<String> modifiers=modifierList(children[0]);
			modifiers.add(children[1].getSymbol());
			return modifiers;
		}else{
			ArrayList<String> modifiers=new ArrayList<String>();
			modifiers.add(children[0].getSymbol());
			return modifiers;
		}
	}
}

@SuppressWarnings("serial") 
class WeederException extends Exception{
	private String weederMessage;
	
	public WeederException(Token token,String weederMessage){
		this.weederMessage="Error: "
				+token.getFileName()
				+"(Line: "
				+token.getLine()
				+", Column: "
				+token.getColumn()
				+") "
				+weederMessage;
	}
	
	public void printWeederMessage(){
		System.err.println(weederMessage);
	}
}
