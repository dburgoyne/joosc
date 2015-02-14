package Parser;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Utilities.StringUtils;

// Argument 1: .cfg.fix file input
// Argument 2: .ast.cfg file output
public class AstGenerator{
	private final static String FINAL_INDICATOR="final";
	private final static String INTERMEDIA_INDICATOR="intermedia";
	private final static String IGNORE_INDICATOR="ignore";
	
	private static String[] readableLines;
	private static int fileLength;
	private static int readIndex;
	
	private static int numTerminal;
	private static int numNonTerminal;
	private static String startSymbol;
	private static int numRule;
	
	public static void main(String[] args){
		if(args.length<2){
			return;
		}
		
		try{
			readableLines=StringUtils.readFile(args[0]).split(System.getProperty("line.separator"));
			System.setOut(new PrintStream(new FileOutputStream(args[1])));
		}catch(IOException e){
			e.printStackTrace();
			return;
		}
		
		fileLength=readableLines.length;
		readIndex=0;
		
		AstTable astTable=new AstTable();
		
		numTerminal=getNewValue();
		for(int i=0;i<numTerminal;i++){
			String[] symbols=getNewLine().split(" ");
			if(symbols[1].equals(FINAL_INDICATOR)){
				astTable.insertFinalTerminal(symbols[0]);
			}else if(symbols[1].equals(INTERMEDIA_INDICATOR)){
				astTable.insertIntermediaTerminal(symbols[0]);
			}else if(symbols[1].equals(IGNORE_INDICATOR)){
			}
		}
		numNonTerminal=getNewValue();
		for(int i=0;i<numNonTerminal;i++){
			astTable.insertNonTerminal(getNewLine());
		}
		startSymbol=getNewLine();
		astTable.setStartSymbol(startSymbol);
		numRule=getNewValue();
		for(int i=0;i<numRule;i++){
			astTable.insertRule(getNewLine());
		}
		
		astTable.generateAst();
	}
	
	private static String getNewLine(){
		String line="";
		while(line.isEmpty()&&readIndex<fileLength){
			line=readableLines[readIndex].split("//")[0];
			readIndex=readIndex+1;
		}
		return line;
	}
	
	private static int getNewValue(){
		String line="";
		while(line.isEmpty()&&readIndex<fileLength){
			line=readableLines[readIndex].split("//")[0];
			readIndex=readIndex+1;
		}
		return Integer.parseInt(line);
	}
}

class AstTable{
	private ArrayList<String> finalTerminals;
	private ArrayList<String> intermediaTerminals;
	private ArrayList<String> nonTerminals;
	private String startSymbol;
	private Map<String,ArrayList<String>> rules;
	private Map<String,ArrayList<String>> relations;
	private Map<String,ArrayList<String>> inverseRelations;
	private Map<String,ArrayList<String>> finalTerminalMapping;
	
	public AstTable(){
		finalTerminals=new ArrayList<String>();
		intermediaTerminals=new ArrayList<String>();
		nonTerminals=new ArrayList<String>();
		rules=new HashMap<String,ArrayList<String>>();
		relations=new HashMap<String,ArrayList<String>>();
		inverseRelations=new HashMap<String,ArrayList<String>>();
		finalTerminalMapping=new HashMap<String,ArrayList<String>>();
	}
	
	public void insertFinalTerminal(String finalTerminal){
		finalTerminals.add(finalTerminal);
	}
	
	public void insertIntermediaTerminal(String intermediaTerminal){
		intermediaTerminals.add(intermediaTerminal);
	}
	
	public void insertNonTerminal(String nonTerminal){
		nonTerminals.add(nonTerminal);
	}
	
	public void setStartSymbol(String startSymbol){
		this.startSymbol=startSymbol;
	}
	
	// This method should be called after all symbols are inserted
	public void insertRule(String rule){
		String[] symbols=rule.split(" ");
		String lhs=symbols[0];
		String newRule=lhs;
		for(int i=1;i<symbols.length;i++){
			String symbol=symbols[i];
			if(finalTerminals.contains(symbol)||intermediaTerminals.contains(symbol)||nonTerminals.contains(symbol)){
				newRule=newRule+" "+symbol;
				ArrayList<String> relation=relations.get(lhs);
				if(relation!=null){
					if(!relation.contains(symbol)){
						relation.add(symbol);
					}
				}else{
					relation=new ArrayList<String>();
					relation.add(symbol);
					relations.put(lhs,relation);
				}
				relation=inverseRelations.get(symbol);
				if(relation!=null){
					if(!relation.contains(lhs)){
						relation.add(lhs);
					}
				}else{
					relation=new ArrayList<String>();
					relation.add(lhs);
					inverseRelations.put(symbol,relation);
				}
			}
		}
		if(rules.containsKey(lhs)){
			rules.get(lhs).add(newRule);
		}else{
			ArrayList<String> newList=new ArrayList<String>();
			newList.add(newRule);
			rules.put(lhs,newList);
		}
	}
	
	public void generateAst(){
		for(String finalTerminal:finalTerminals){
			inverseTerminalMapping(finalTerminal,finalTerminal);
		}
		for(String symbol:nonTerminals){
			for(String rule:rules.get(symbol)){
				buildAstTree(rule.split(" "));
			}
		}
		AstNode.printAstTree();
	}
	
	private void inverseTerminalMapping(String symbol,String finalTerminal){
		ArrayList<String> parents=inverseRelations.get(symbol);
		if(parents!=null){
			for(String parent:parents){
				ArrayList<String> mapping=finalTerminalMapping.get(parent);
				if(mapping!=null){
					if(!mapping.contains(finalTerminal)){
						mapping.add(finalTerminal);
						inverseTerminalMapping(parent,finalTerminal);
					}
				}else{
					mapping=new ArrayList<String>();
					mapping.add(finalTerminal);
					finalTerminalMapping.put(parent,mapping);
					inverseTerminalMapping(parent,finalTerminal);
				}
			}
		}
	}
	
	private void buildAstTree(String[] rule){
		String lhs=rule[0];
		AstNode parent=NonTerminalNode.acquireAstNode(lhs);
		ArrayList<AstNode> list=new ArrayList<AstNode>();
		for(int i=1;i<rule.length;i++){
			String symbol=rule[i];
			if(symbolOverlap(lhs,symbol)){
				parent.cloneAstNode(symbol);
				list.add(parent);
			}else if(finalTerminals.contains(symbol)){
				list.add(TerminalNode.acquireAstNode(symbol));
			}else{
				list.add(NonTerminalNode.acquireAstNode(symbol));
			}
		}
		if(list.size()!=1||!symbolOverlap(list.get(0).getSymbol(),lhs)){
			parent.addRule(list);
		}
	}
	
	private boolean symbolOverlap(String parent,String child){
		return finalTerminalMapping.containsKey(parent)
				&&finalTerminalMapping.containsKey(child)
				&&finalTerminalMapping.get(parent).size()
				==finalTerminalMapping.get(child).size();
	}
}

abstract class AstNode{
	protected static ArrayList<AstNode> nodes=new ArrayList<AstNode>();
	protected static ArrayList<String> symbols=new ArrayList<String>();
	protected static ArrayList<Boolean> clone=new ArrayList<Boolean>();
	
	private String symbol;
	private ArrayList<ArrayList<AstNode>> children;
	
	public static AstNode acquireAstNode(String symbol){
		return null;
	}
	
	public static void printAstTree(){
		for(int i=0;i<nodes.size();i++){
			if(!clone.get(i)){
				AstNode node=nodes.get(i);
				for(ArrayList<AstNode> list:node.children){
					System.out.print(node.symbol);
					for(AstNode child:list){
						System.out.print(" "+child.symbol);
					}
					System.out.println();
				}
			}
		}
	}
	
	public AstNode(String symbol){
		this.symbol=symbol;
		children=new ArrayList<ArrayList<AstNode>>();
	}
	
	public void cloneAstNode(String symbol){
		if(!symbols.contains(symbol)){
			AstNode node=nodes.get(symbols.indexOf(this.symbol));
			nodes.add(node);
			symbols.add(symbol);
			clone.add(true);
		}
	}
	
	public void addRule(ArrayList<AstNode> child){
		if(!children.contains(child)){
			children.add(child);
		}
	}
	
	public String getSymbol(){
		return symbol;
	}
}

class TerminalNode extends AstNode{
	public static AstNode acquireAstNode(String symbol){
		int index=symbols.indexOf(symbol);
		if(index<0){
			AstNode node=new TerminalNode(symbol);
			nodes.add(node);
			symbols.add(symbol);
			clone.add(false);
			return node;
		}else{
			return nodes.get(index);
		}
	}
	
	public TerminalNode(String symbol){
		super(symbol);
	}
}

class NonTerminalNode extends AstNode{
	public static AstNode acquireAstNode(String symbol){
		int index=symbols.indexOf(symbol);
		if(index<0){
			AstNode node=new NonTerminalNode(symbol);
			nodes.add(node);
			symbols.add(symbol);
			clone.add(false);
			return node;
		}else{
			return nodes.get(index);
		}
	}
	
	public NonTerminalNode(String symbol){
		super(symbol);
	}
}
