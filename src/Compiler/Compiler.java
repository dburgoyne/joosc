package Compiler;

import java.io.IOException;
import java.util.List;

import AbstractSyntax.Program;
import AbstractSyntax.TypeDecl;
import Exceptions.CodeGenerationException;
import Exceptions.ImportException;
import Exceptions.NameConflictException;
import Exceptions.NameLinkingException;
import Exceptions.ReachabilityException;
import Exceptions.TypeCheckingException;
import Exceptions.TypeLinkingException;
import Parser.ParseException;
import Parser.ParseTable;
import Parser.ParseTree;
import Scanner.ScanException;
import Scanner.Token;
import Types.Hierarchy;
import Types.MemberSet;
import Utilities.Cons;
import Utilities.StringUtils;

// Runs the entire compilation process.

public class Compiler {

	protected static ParseTable parseTable;
	protected static ParseTree[] parseTrees;
	public static final String OUTPUT_DIR = "output";

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
    	
        // Generate the AST.
		Program program = new Program(parseTrees);
    	try {
    		// Environment building pass
    		program.buildEnvironment(null);
    		
    		// Type linking pass.
    		Cons<TypeDecl> allTypeDecls = program.getAllTypeDecls();
    		program.linkTypes(allTypeDecls);
    		
    		// Hierarchy building pass.
    		Hierarchy h = new Hierarchy(allTypeDecls);
    		List<TypeDecl> sortedTypes = h.topologicalSort();
    		
    		// Build method/field/ctor/ancestor sets in topological order.
    		{
    			int tid = 0;
	    		for (TypeDecl ty : sortedTypes) {
	    			ty.buildMemberSet();
	    			ty.setTypeID(++tid);
	    		}
    		}
    		
    		// Name resolution pass.
    		program.linkNames(null, false, null, null, false);
    		
    		// Type checking pass.
    		program.checkTypes();
    		
    		// Reachability analysis pass.
    		program.checkReachability(true);
    		
    		// Build Vtable schemas and field indices.
    		for (TypeDecl ty : sortedTypes) {
    			ty.buildSchema();
    		}
    		    		
    		// Validate existence of entry point.
    		if (Program.staticIntTest == null || Program.staticIntTest.getDeclaringType() != allTypeDecls.head) {
    			throw new CodeGenerationException.NoStaticIntTest(allTypeDecls.head);
    		}
    		
    		// Code generation pass.
    		program.generateCode(null, null);
    		
    		
    	} catch (Exception e) {
			if (e instanceof NameConflictException
			 || e instanceof ImportException
			 || e instanceof TypeLinkingException
			 || e instanceof Hierarchy.CycleDetected
			 || e instanceof MemberSet.Exception
			 || e instanceof NameLinkingException
			 || e instanceof TypeCheckingException
			 || e instanceof ReachabilityException
			 || e instanceof CodeGenerationException) {
    			System.err.println(e.getMessage());
    			failed = true;
			} else {
				e.printStackTrace();
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
