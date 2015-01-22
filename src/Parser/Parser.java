package Parser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

//Argument 1: .lr1 file input
//Argument 2: test file input
public class Parser{
	public static void main(String[] args){
		if(args.length<2){
			return;
		}
		
		// Read lr1 file
		String lrSource;
		String[] lrLines;
		try{
			lrSource=new String(Files.readAllBytes(Paths.get(args[0])),StandardCharsets.UTF_8);
		}catch (IOException e){
			e.printStackTrace();
			return;
		}
		lrLines=lrSource.split(System.getProperty("line.separator"));
		
		// Store parse table
		int numTerminal=Integer.parseInt(lrLines[0]);
		int numNonTerminal=Integer.parseInt(lrLines[numTerminal+1]);
		int numRule=Integer.parseInt(lrLines[numTerminal+numNonTerminal+3]);
		int numState=Integer.parseInt(lrLines[numTerminal+numNonTerminal+numRule+4]);
		int numTransition=Integer.parseInt(lrLines[numTerminal+numNonTerminal+numRule+5]);
		ParseTable parseTable=new ParseTable(numTerminal+numNonTerminal,numRule,numState,numTransition);
		for(int i=0;i<numTerminal;i++){
			parseTable.addSymbol(lrLines[i+1]);
		}
		for(int i=0;i<numNonTerminal;i++){
			parseTable.addSymbol(lrLines[i+numTerminal+2]);
		}
		for(int i=0;i<numRule;i++){
			parseTable.addRule(lrLines[i+numTerminal+numNonTerminal+4]);
		}
		for(int i=0;i<numTransition;i++){
			parseTable.addTransition(lrLines[i+numTerminal+numNonTerminal+numRule+6]);
		}
		//parseTable.print();
		
		// Read tokens input
		String inputSource;
		String[] tokens;
		try{
			inputSource=new String(Files.readAllBytes(Paths.get(args[1])),StandardCharsets.UTF_8);
		}catch (IOException e){
			e.printStackTrace();
			return;
		}
		tokens=inputSource.split(" ");
		
		// Parsing
		parseTable.parse(tokens);
	}
}

class Transition{
	public int m_from;
	public int m_symbol;
	public boolean m_shift;
	public int m_to;
	
	public Transition(int from,int symbol,boolean shift,int to){
		m_from=from;
		m_symbol=symbol;
		m_shift=shift;
		m_to=to;
	}
}

class ParseTable{
	private ArrayList<String> symbols;
	private ArrayList<ArrayList<Integer>> rules;
	private ArrayList<ArrayList<Transition>> transitions;
	
	public ParseTable(int numSymbol,int numRule,int numState,int numTransition){
		symbols=new ArrayList<String>(numSymbol);
		rules=new ArrayList<ArrayList<Integer>>(numRule);
		transitions=new ArrayList<ArrayList<Transition>>(numState);
		for(int i=0;i<numState;i++){
			transitions.add(new ArrayList<Transition>());
		}
	}
	
	public void addSymbol(String symbol){
		symbols.add(symbol);
	}
	
	public void addRule(String rule){
		String[] ruleList=rule.split(" ");
		ArrayList<Integer> item=new ArrayList<Integer>();
		for(String symbol:ruleList){
			item.add(symbols.indexOf(symbol));
		}
		rules.add(item);
	}
	
	public void addTransition(String transition){
		String[] transitionList=transition.split(" ");
		transitions.get(Integer.parseInt(transitionList[0])).add(new Transition(Integer.parseInt(transitionList[0]),
				symbols.indexOf(transitionList[1]),
				transitionList[2].equals("shift"),
				Integer.parseInt(transitionList[3])));
	}
	
	public void print(){
		for(String symbol:symbols){
			System.out.println(symbol);
		}
		for(ArrayList<Integer> rule:rules){
			for(int index:rule){
				System.out.print(symbols.get(index)+" ");
			}
			System.out.println();
		}
		for(ArrayList<Transition> state:transitions){
			for(Transition transition:state){
				System.out.print(transition.m_from+" "+symbols.get(transition.m_symbol)+" ");
				if(transition.m_shift){
					System.out.print("Shift");
				}else{
					System.out.print("Reduce");
				}
				System.out.println(" "+transition.m_to);
			}
		}
	}
	
	public void parse(String[] tokenList){
		Deque<Integer> tokens=new ArrayDeque<Integer>();
		Deque<Integer> stack=new ArrayDeque<Integer>();
		Deque<Integer> steps=new ArrayDeque<Integer>();
		
		for(int i=tokenList.length-1;i>=0;i--){
			tokens.push(symbols.indexOf(tokenList[i]));
		}
		stack.push(0);
		
		while(!tokens.isEmpty()){
			Transition transition=searchTransition(stack.peek(),tokens.peek());
			if(transition.m_shift){
				stack.push(tokens.pop());
				stack.push(transition.m_to);
			}else{
				ArrayList<Integer> rule=rules.get(transition.m_to);
				tokens.push(rule.get(0));
				for(int i=0;i<rule.size()-1;i++){
					stack.pop();
					stack.pop();
				}
				steps.push(transition.m_to);
			}
		}
		
		while(!steps.isEmpty()){
			printRule(rules.get(steps.pop()));
		}
	}
	
	private Transition searchTransition(int state,int token){
		for(Transition transition:transitions.get(state)){
			if(transition.m_symbol==token){
				return transition;
			}
		}
		return null;
	}
	
	private void printRule(ArrayList<Integer> rule){
		System.out.print(symbols.get(rule.get(0))+" ->");
		for(int i=1;i<rule.size();i++){
			System.out.print(" "+symbols.get(rule.get(i)));
		}
		System.out.println();
	}
}
