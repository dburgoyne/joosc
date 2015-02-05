package Parser;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Stack;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
		String startSymbol=lrLines[numTerminal+numNonTerminal+2];
		int numRule=Integer.parseInt(lrLines[numTerminal+numNonTerminal+3]);
		int numState=Integer.parseInt(lrLines[numTerminal+numNonTerminal+numRule+4]);
		int numTransition=Integer.parseInt(lrLines[numTerminal+numNonTerminal+numRule+5]);
		ParseTable parseTable=new ParseTable(numTerminal+numNonTerminal,numRule,numState,numTransition);
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
        tokens.add(new Token("$", Scanner.TokenType.EOF, args[1], -1, -1));

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
	public boolean shift;
	// Target is a state number (for shifts) or a rule number (for reductions).
	public int target;
	
	public Transition(boolean shift, int target){
		this.shift = shift;
		this.target = target;
	}
}

class ParseTable{
	private List<List<String>> rules;
	private Map<Pair<Integer, String>, Transition> transitions;
	
	public ParseTable(int numSymbol,int numRule,int numState,int numTransition){
		rules = new ArrayList<List<String>>(numRule);
		transitions = new HashMap<Pair<Integer, String>, Transition>();
	}
	
	public void addRule(String rule){
		List<String> ruleList= Arrays.asList(rule.split(" "));
		rules.add(ruleList);
	}
	
	public void addTransition(String transition){
		String[] transitionList=transition.split(" ");
		int sourceState = Integer.parseInt(transitionList[0]);
		String symbol = transitionList[1];
		boolean shift = transitionList[2].equals("shift");
		int target = Integer.parseInt(transitionList[3]);
		
		transitions.put(new Pair<Integer, String>(sourceState, symbol), new Transition(shift, target));
	}
	
	// Prints rightmost derivation.
	/*public void parse(String[] tokenList){
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
	}*/
	
	
	public ParseTree parse(List<Token> tokenList){
        Stack<Integer> stateStack  = new Stack<Integer>();
        Stack<ParseTree> symbolStack  = new Stack<ParseTree>();

        stateStack.push(0);
        
        while (!tokenList.isEmpty()) {
        	// Read one symbol of input.
        	Token token = tokenList.get(0);
            Transition transition = transitions.get(new Pair<Integer, String>(stateStack.peek(), token.getCfgName()));

    		System.out.println(stateStack.peek()+ " " + token.getCfgName());
            // Reduce as long as we are able to.
        	while (!transition.shift) {
        		List<String> rule = rules.get(transition.target);
        		String lhs = rule.get(0);
        		System.out.println("Reducing to " + lhs);
        		
        		List<ParseTree> children = new ArrayList<ParseTree>();
        		for (int i = 1; i < rule.size(); i++) {
        			// Pop both stacks.
        			stateStack.pop();
        			children.add(0, symbolStack.pop());
        		}
        		
        		// Build a ParseTree representing the LHS of the rule and push it.
        		ParseTree newNode = new NonTerminal(lhs, children.toArray(new ParseTree[0]));
        		symbolStack.push(newNode);
        		
        		// Move into the new state.
        		stateStack.push(transitions.get(new Pair<Integer, String>(stateStack.peek(), newNode.getSymbol())).target);
        		transition = transitions.get(new Pair<Integer, String>(stateStack.peek(), token.getCfgName()));
        	}
        	System.out.println("Shifting " + token.getCfgName());
        	stateStack.push(transition.target);
        	symbolStack.push(new Terminal(token));
        	tokenList.remove(0);
        }
        
        assert symbolStack.size() == 1;
        return symbolStack.peek();
	}
}
