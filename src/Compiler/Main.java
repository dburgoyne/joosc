package Compiler;

import java.io.PrintStream;

// Main entry point for assignments.

public class Main {

    // Argument 1: .lr1 file input
    // Arguments 2, 3, ..., n: List of Joos files
    // Exits with 0 if valid Joos, 42 if not, 1 if bug. 
    public static void main(String[] args) throws Exception {
        if (args.length < 2){
            System.out.println("Error: Please provide at least one Joos input file.");
            return;
        }
        
        System.setOut(new PrintStream(new java.io.OutputStream() {
            public void write(int b) {
                // Disable tests' output
            }
        }));
        
    	String[] filenames = new String[args.length - 1];
    	for (int i = 0; i < args.length - 1; i++) {
    		filenames[i] = args[i + 1];
    	}
    	
    	Assembler.cleanOutputDirectory();  // TODO Make sure this works with Marmoset runner
    	int retval = Compiler.compile(args[0], filenames);
    	if (retval != 0) {
    		System.err.println("Compilation failed with exit status " + retval);
    		System.exit(retval);
    	}
    	retval = Assembler.assemble();
    	if (retval != 0) {
    		System.err.println("Assembly failed with exit status " + retval);
    		System.exit(retval);
    	}
		System.out.println("Execution succeeded!");
    	//System.exit(Compiler.compile(args[0], filenames));
    }
}
