package Compiler;

import java.io.IOException;
import java.util.List;

import AbstractSyntax.ASTNode;
import AbstractSyntax.NameConflictException;
import AbstractSyntax.Program;
import Parser.ParseException;
import Parser.ParseTable;
import Parser.ParseTree;
import Scanner.ScanException;
import Scanner.Token;
import Utilities.StringUtils;

// Runs the entire compilation process.

public class Compiler {

	protected static ParseTable parseTable;
	protected static ParseTree[] parseTrees;

    //Argument 1: .lr1 file input
    //Argument 2: test file input
    // Returns 0 if compilation succeeded, 42 if it did not, 1 if it crashed. 
    public static int compile(String lr1File, String... joosFiles) {
    	buildParseTable(lr1File);
        
    	parseTrees = new ParseTree[joosFiles.length];
    	boolean failed = false;
    	for (int i = 0; i < joosFiles.length; i++) {
    		try {
    			parseTrees[i] = buildParseTree(joosFiles[i]);
    		} catch (Exception e) {
    			if (e instanceof ParseException
    			 || e instanceof ScanException) {
    				System.err.println(e.getMessage());
	    			failed = true;
    			} else {
    				//IOExceptions trigger this case.
    				return 1;
    			}
    		}
    	}
        
    	if (failed) {
    		return 42;
    	}
    	
        // Generate the AST
		ASTNode program = new Program(parseTrees);
    	try {
    		program.buildEnvironment(null);
    	} catch (Exception e) {
			if (e instanceof NameConflictException) {
    			System.err.println(e.getMessage());
    			failed = true;
			} else {
				return 1;
			}
		}
        
        if (failed) {
    		return 42;
    	}
        
        return 0;
    }
    
    // Given the name of a Joos source file, produce a valid parse tree for it.
    public static ParseTree buildParseTree(String joosFile) throws IOException, ScanException, ParseException {
    	// Read tokens input
        String inputSource;
        try{
            inputSource=StringUtils.readFile(joosFile);
        }catch (IOException e){
            e.printStackTrace();
            throw e;
        }
        
        String[] inputLines = inputSource.split("\n");
        for (int i = 0; i < inputLines.length; i++)
            for (int j = 0; j < inputLines[i].length(); j++)
                if (inputLines[i].charAt(j) < 0 || inputLines[i].charAt(j) > 127) {
                    String error = String.format(
                    		"Error: non-7-bit ASCII character 0x%02x at file %s, line %d, column %d\n", 
                            (int)inputLines[i].charAt(j), 
                            joosFile, (i+1), (j+1));
                    throw new ScanException(error);
                }

    	// Scanning
    	List<Token> tokens = Scanner.Scanner.scan(joosFile, inputSource);
    	tokens.add(new Token("$", Scanner.TokenType.EOF, joosFile, -1, -1));
    	
        // Parsing
        ParseTree pt = parseTable.parse(tokens);
        
        // Weeding
        Weeder.Weeder.weed(pt);
        
        return pt;
    }
    
    // Given the name of a .lr1 file, generate a parse table from it.
    public static void buildParseTable(String lr1File) {
    	// Read lr1 file
    	String lrSource;
    	String[] lrLines;
    	
        try{
            lrSource=StringUtils.readFile(lr1File);
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
        parseTable=new ParseTable(numTerminal+numNonTerminal,numRule,numState,numTransition);
        for(int i=0;i<numRule;i++){
            parseTable.addRule(lrLines[i+numTerminal+numNonTerminal+4]);
        }
        for(int i=0;i<numTransition;i++){
            parseTable.addTransition(lrLines[i+numTerminal+numNonTerminal+numRule+6]);
        }
    }

}
