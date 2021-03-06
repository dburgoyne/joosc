package Parser;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

import Compiler.Compiler;

public class RunParserTests {

    final static String LR1_FILE = "src/Parser/joos1w.lr1"; 
    final static String POSITIVES_DIR = "test/marmoset/a1/positive/";
    final static String NEGATIVES_DIR = "test/marmoset/a1/negative/";
    
    public static void main(String[] args) {
        PrintStream stdout = System.out;
        PrintStream stderr = System.err;
        PrintStream redirect = new PrintStream(new java.io.OutputStream() {
            public void write(int b) {
                // Disable Tests' error output
            }
        });
        System.setOut(redirect);
        System.setErr(redirect);

        ArrayList<String> positives = new ArrayList<String>();
        int positives_passed = 0;
        ArrayList<String> negatives = new ArrayList<String>();
        int negatives_passed = 0;

        for (File entry : new File(POSITIVES_DIR).listFiles())
            if (entry.getName().endsWith(".java"))
                positives.add(entry.getName());
        for (File entry : new File(NEGATIVES_DIR).listFiles())
            if (entry.getName().endsWith(".java"))
                negatives.add(entry.getName());
        
        stdout.println("=== TESTING NEGATIVES ===");
        for (String filename : negatives) {
            stdout.printf("%-60s", "-> " + filename + ":");
        	int retval = Compiler.compile(LR1_FILE, NEGATIVES_DIR + filename);
        	if (retval == 42) {
        		stdout.println("+++ PASS +++");
                negatives_passed++;
        	} else {
        		stderr.println("--- FAIL ---");
            }
        }
        stdout.println("=== Passed " + negatives_passed + " / " + negatives.size() + " tests ===");
        
        stdout.println("\n=== TESTING POSITIVES ===");
        for (String filename : positives) {
            stdout.printf("%-60s", "-> " + filename + ":");
            int retval = Compiler.compile(LR1_FILE, POSITIVES_DIR + filename);
        	if (retval == 0) {
        		stdout.println("+++ PASS +++");
                positives_passed++;
        	} else {
        		stderr.println("--- FAIL ---");
            }
        }
        
        stdout.println("=== Passed " + positives_passed + " / " + positives.size() + " tests ===");
        
        stdout.println("=== Total passed " + (positives_passed+negatives_passed) + " / " + (positives.size()+negatives.size()) + " tests ===");

    }
    
    
}
