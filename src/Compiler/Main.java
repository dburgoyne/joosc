package Compiler;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import Parser.ParseException;

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
        
        try {
        	String[] filenames = new String[args.length - 1];
        	for (int i = 0; i < args.length - 1; i++) {
        		filenames[i] = args[i + 1];
        	}
        	
        	Compiler.compile(args[0], filenames);
        } catch (IOException e) {
        	System.err.println(e.getMessage());
        	System.exit(1);
        } catch (Exception e) {
        	System.err.println(e.getMessage());
        	throw(e);
        	//System.exit(42);
        }
    }
}
