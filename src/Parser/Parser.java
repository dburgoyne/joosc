package Parser;

import java.io.IOException;
import java.util.List;

import Utilities.StringUtils;
import Scanner.ScanException;
import Scanner.Token;

//Argument 1: .lr1 file input
//Argument 2: test file input
public class Parser{
	public static void main(String[] args) throws ParseException, ScanException {
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
		
		// Check char range
        String[] inputLines = inputSource.split("\n");
        for (int i = 0; i < inputLines.length; i++)
            for (int j = 0; j < inputLines[i].length(); j++)
                if (inputLines[i].charAt(j) < 0 || inputLines[i].charAt(j) > 127) {
                	String error = String.format(
                			"Error: non-7-bit ASCII character 0x%02x at file %s, line %d, column %d", 
                            (int)inputLines[i].charAt(j), 
                            args[1], (i+1), (j+1));
                	throw new ParseException(error);
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
            @Override public void visit(String rule, ParseTree... children) throws ParseException {
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

