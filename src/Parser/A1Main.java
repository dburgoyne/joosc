package Parser;

import java.io.IOException;
import java.io.PrintStream;

import Compiler.Compiler;
import Parser.ParseException;
import Scanner.ScanException;


// Main entry point for Assignment 1

public class A1Main {

    //Argument 1: .lr1 file input
    //Argument 2: test file input
    // Exits with 0 if syntactically valid Joos, 42 if not, 1 if bug. 
    public static void main(String[] args) throws ParseException {
        if(args.length<2){
            System.out.println("Error: Please provide a Joos input file.");
            return;
        }
        System.setOut(new PrintStream(new java.io.OutputStream() {
            public void write(int b) {
                // Disable Tests' output
            }
        }));
        
        try {
        	Compiler.compile(args[0], args[1]);
        } catch (IOException e) {
        	System.err.println(e.getMessage());
        	System.exit(1);
        } catch (Exception e) {
        	System.err.println(e.getMessage());
        	System.exit(42);
        }
    }
}
