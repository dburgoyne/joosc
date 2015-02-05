package Weeder;

import java.util.ArrayList;

import Parser.ParseTree;
import Scanner.Token;

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
			if(!validSevenBit(token.getFileName())){
				throw new WeederException(token,"Input contains an invalid 7-bit ASCII character");
			}
		}else{
			String symbol=tree.getSymbol();
			ParseTree[] children=tree.getChildren();
			// Class declaration
			if(symbol.equals("ClassDeclaration")){
				for(ParseTree child:children){
					if(child.getSymbol().equals("Modifiers")){
						ArrayList<String> modifiers=classModifierList(child);
						if(modifiers.contains("abstract")&&modifiers.contains("final")){
							assert children[2].isTerminal();
							throw new WeederException(children[2].getToken(),"Class is both abstract and final");
						}
					}else{
						weedBranch(child);
					}
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
	
	// A class cannot be both abstract and final
	private ArrayList<String> classModifierList(ParseTree tree){
		ParseTree[] children=tree.getChildren();
		if(children[0].getSymbol().equals("Modifiers")){
			ArrayList<String> modifiers=classModifierList(children[0]);
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
