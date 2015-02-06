package Parser;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import Scanner.Token;
import Utilities.StringUtils;

// Main entry point for Assignment 1

public class A1Main {

    //Argument 1: .lr1 file input
    //Argument 2: test file input
    // Exits with 0 if syntactically valid Joos, 42 if not, 1 if bug. 
    public static void main(String[] args) {
        if(args.length<2){
            System.out.println("Error: Please provide a Joos input file.");
            return;
        }
        System.setOut(new PrintStream(new java.io.OutputStream() {
            public void write(int b) {
                // Disable Tests' output
            }
        }));
        
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
        
        // Read tokens input
        String inputSource;
        try{
            inputSource=StringUtils.readFile(args[1]);
        }catch (IOException e){
            e.printStackTrace();
            System.exit(1);
            return;
        }
        
        String[] inputLines = inputSource.split("\n");
        for (int i = 0; i < inputLines.length; i++)
            for (int j = 0; j < inputLines[i].length(); j++)
                if (inputLines[i].charAt(j) < 0 || inputLines[i].charAt(j) > 127) {
                    System.err.printf("Error: non-7-bit ASCII character 0x%02x at " +
                                      "file %s, line %d, column %d\n", 
                                      (int)inputLines[i].charAt(j), 
                                      args[1], (i+1), (j+1));
                    System.exit(42);
                }
        
        // Scanning
        List<Token> tokens = Scanner.Scanner.scan(args[1], inputSource);
      if (tokens == null) System.exit(42);
        tokens.add(new Token("$", Scanner.TokenType.EOF, args[1], -1, -1));

        // Parsing
        ParseTree pt = parseTable.parse(tokens);
      if (pt == null) System.exit(42);
    }

}
