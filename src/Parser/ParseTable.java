package Parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import Scanner.Token;

public class ParseTable{
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
	
	public ParseTree parse(List<Token> tokenList) throws ParseException{
        Stack<Integer> stateStack  = new Stack<Integer>();
        Stack<ParseTree> symbolStack  = new Stack<ParseTree>();

        stateStack.push(0);
        
        while (!tokenList.isEmpty()) {
        	// Read one symbol of input.
        	Token token = tokenList.get(0);
            Transition transition = transitions.get(new Pair<Integer, String>(stateStack.peek(), token.getCfgName()));

            // Reduce as long as we are able to.
        	while (transition != null && !transition.shift) {
        		List<String> rule = rules.get(transition.target);
        		String lhs = rule.get(0);
        		
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
        	
        	if (transition == null) {
        		String error = String.format(
        				"ERROR: unexpected token \"%s\"\n at file %s, line %d, column %d\n", 
                        token.getLexeme(),
                        token.getFileName(),
                        token.getLine(),
                        token.getColumn());
        	    throw new ParseException(error);
        	}
        	
        	stateStack.push(transition.target);
        	symbolStack.push(new Terminal(token));
        	tokenList.remove(0);
        }
        
        // Since S -> CompilationUnit EOF is the start symbol,
        // contents of symbolStack should now be [CompilationUnit, EOF].
        return symbolStack.firstElement();
	}
}