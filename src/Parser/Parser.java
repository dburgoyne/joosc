package Parser;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import Utilities.StringUtils;
import Scanner.Token;

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
			lrSource=StringUtils.readFile(args[0]);
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
		try{
			inputSource=StringUtils.readFile(args[1]);
		}catch (IOException e){
			e.printStackTrace();
			return;
		}
		
		// Scanning
        List<Token> tokens = Scanner.Scanner.scan(args[1], inputSource);
		
		// Parsing
		ParseTree pt = parseTable.parse(tokens);
		
		// Print parse tree
		class PrintPT implements ParseTree.Visitor {
		    String linePrefix;
		    public PrintPT(String lp) { linePrefix = lp; }
            @Override public void visit(Token t) {
                System.out.print(linePrefix);
                System.out.println(t);
            }
            @Override public void visit(String rule, ParseTree... children) {
                System.out.print(linePrefix);
                System.out.println(rule);
              if (children.length == 0) return;
              
                String modifiedPrefix = linePrefix.replace('-', ' ')
                                                  .replace('+', ' ');
                PrintPT butLast = new PrintPT(modifiedPrefix + " |- ");
                PrintPT last    = new PrintPT(modifiedPrefix + " +- ");
                for (int i = 0; i < children.length - 1; i++) {
                    children[i].visit(butLast);
                }
                children[children.length - 1].visit(last);
            }
		}
		pt.visit(new PrintPT(""));
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
	
	// Prints rightmost derivation.
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
	
	// Idea: replace index-shuffling above with trees with said indices embedded.
	private static interface IxTree extends ParseTree { int getIndex(); }
	private static class IxTerminal extends Terminal implements IxTree {
	    private final int number;
        public int getIndex() { return number; }
	    public IxTerminal(int tokenNum, Token token) { 
	        super(token);
	        number = tokenNum; 
	    }
    }   
	private static class IxNonTerminal extends NonTerminal implements IxTree {
        public int getIndex() { return this.ruleNum; }
        private final int ruleNum;
	    public IxNonTerminal(ParseTable pt, int rule, ParseTree... children) {
            super(pt.ruleToString(rule), children);
            ruleNum = rule;
        }
        @Override public void visit(Visitor v) {
            v.visit(rule, children);
        }
	}
	
	public ParseTree parse(List<Token> tokenList){
        Deque<IxTree> tokens = new ArrayDeque<IxTree>();
        Deque<IxTree> stack  = new ArrayDeque<IxTree>();
        Deque<IxTree> roots  = new ArrayDeque<IxTree>();
        
        for(Token tok : tokenList){
            tokens.push(new IxTerminal(symbols.indexOf(tok.getCfgName()), tok));
        }
        stack.push(new IxNonTerminal(this, 0));
        
        while (!tokens.isEmpty()) {
            Transition transition = searchTransition(stack.peek().getIndex(),
                                                     tokens.peek().getIndex());
            if(transition.m_shift){
                stack.push(tokens.pop());
                stack.push(new IxNonTerminal(this, transition.m_to));
            }else{
                ArrayList<Integer> rule=rules.get(transition.m_to);
                tokens.push(new IxNonTerminal(this, rule.get(0)));
                for(int i=0;i<rule.size()-1;i++){
                    stack.pop();
                    roots.push(stack.pop());
                }
                IxTree newRoot = new IxNonTerminal(this, transition.m_to,
                                                   roots.toArray(new IxTree[0]));
                roots.clear();
                roots.push(newRoot);
            }
        }
        
        assert roots.size() == 1;
        return roots.peek();
	}
	
	private String ruleToString(int ruleNum) {
	    ArrayList<Integer> rule = rules.get(ruleNum);
        StringBuilder sb = new StringBuilder(symbols.get(rule.get(0))).append(" ->");
        for(int i=1;i<rule.size();i++){
            sb.append(' ').append(symbols.get(rule.get(i)));
        }
        return sb.toString();
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
