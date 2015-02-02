package Parser;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import Utilities.StringUtils;

//Argument 1: .cfg.readable file input
//Argument 2: .cfg file output
public class CfgGenerator{
	public final static String COMMENT_PREFIX="//";
	
	public static void main(String[] args){
		if(args.length<2){
			return;
		}
		
		// Read .cfg.readable file
		String readableSource;
		String[] readableLines;
		try{
			readableSource=StringUtils.readFile(args[0]);
			System.setOut(new PrintStream(new FileOutputStream(args[1])));
		}catch(IOException e){
			e.printStackTrace();
			return;
		}
		readableLines=readableSource.split(System.getProperty("line.separator"));
		
		// Initial data structure
		SymbolTable symbolTable=new SymbolTable();
		
		// Store context free grammar
		for(String readableLine:readableLines){
			readableLine=readableLine.trim();
			
			if(readableLine.length()<2||readableLine.substring(0,2).equals(COMMENT_PREFIX)){
				continue;
			}
			symbolTable.insertRule(readableLine);
		}
		
		// Output
		symbolTable.generateCfg();
	}
}

class SymbolTable{
	private ArrayList<String> m_rules;
	private ArrayList<String> m_terminals;
	private ArrayList<String> m_nonTerminals;
	
	public SymbolTable(){
		m_rules=new ArrayList<String>();
		m_terminals=new ArrayList<String>();
		m_nonTerminals=new ArrayList<String>();
	}
	
	public void insertRule(String rule){
		String[] symbols=rule.split(" ");
		m_rules.add(rule);
		insertNonTerminalSymbol(symbols[0]);
		for(int i=1;i<symbols.length;i++){
			insertSymbol(symbols[i]);
		}
	}
	
	public void generateCfg(){
		System.out.println(m_terminals.size());
		for(String symbol:m_terminals){
			System.out.println(symbol);
		}
		System.out.println(m_nonTerminals.size());
		for(String symbol:m_nonTerminals){
			System.out.println(symbol);
		}
		System.out.println(m_nonTerminals.get(0));
		System.out.println(m_rules.size());
		for(String rule:m_rules){
			System.out.println(rule);
		}
	}
	
	private void insertSymbol(String symbol){
		if(m_terminals.indexOf(symbol)<0&&m_nonTerminals.indexOf(symbol)<0){
			m_terminals.add(symbol);
		}
	}
	
	private void insertNonTerminalSymbol(String symbol){
		if(m_nonTerminals.indexOf(symbol)<0){
			m_terminals.remove(symbol);
			m_nonTerminals.add(symbol);
		}
	}
}
