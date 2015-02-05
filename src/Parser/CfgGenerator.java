package Parser;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
		
		// Symbol checking
		symbolTable.check();
	}
}

class SymbolTable{
	private int m_numRule;
	private Map<String,ArrayList<String>> m_rules;
	private ArrayList<String> m_terminals;
	private ArrayList<String> m_nonTerminals;
	
	private boolean[] m_terminalReachable;
	private boolean[] m_nonTerminalReachable;
	
	public SymbolTable(){
		m_numRule=0;
		m_rules=new HashMap<String,ArrayList<String>>();
		m_terminals=new ArrayList<String>();
		m_nonTerminals=new ArrayList<String>();
	}
	
	public void insertRule(String rule){
		String[] symbols=rule.split(" ");
		if(!m_rules.containsKey(symbols[0])){
			m_rules.put(symbols[0],new ArrayList<String>());
		}
		m_rules.get(symbols[0]).add(rule);
		insertNonTerminalSymbol(symbols[0]);
		for(int i=1;i<symbols.length;i++){
			insertSymbol(symbols[i]);
		}
		m_numRule=m_numRule+1;
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
		System.out.println(m_numRule);
		for(String symbol:m_nonTerminals){
			ArrayList<String> rules=m_rules.get(symbol);
			for(String rule:rules){
				System.out.println(rule);
			}
		}
	}
	
	public void check(){
		m_terminalReachable=new boolean[m_terminals.size()];
		m_nonTerminalReachable=new boolean[m_nonTerminals.size()];
		checkBranch(m_nonTerminals.get(0));
		for(int i=0;i<m_terminals.size();i++){
			if(!m_terminalReachable[i]){
				System.err.println("Warning: terminal symbol '"+m_terminals.get(i)+"' is unreachable.");
			}
		}
		for(int i=0;i<m_nonTerminals.size();i++){
			if(!m_nonTerminalReachable[i]){
				System.err.println("Warning: non-terminal symbol '"+m_nonTerminals.get(i)+"' is unreachable.");
			}
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
	
	private void checkBranch(String symbol){
		int nonTerminalIndex=m_nonTerminals.indexOf(symbol);
		if(nonTerminalIndex<0){
			m_terminalReachable[m_terminals.indexOf(symbol)]=true;
		}else{
			if(!m_nonTerminalReachable[nonTerminalIndex]){
				m_nonTerminalReachable[nonTerminalIndex]=true;
				ArrayList<String> rules=m_rules.get(symbol);
				for(String rule:rules){
					String[] symbols=rule.split(" ");
					for(int i=1;i<symbols.length;i++){
						checkBranch(symbols[i]);
					}
				}
			}
		}
	}
}
