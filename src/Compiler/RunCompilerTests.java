package Compiler;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;


public class RunCompilerTests {

    final static String LR1_FILE = "src/Parser/joos1w.lr1"; 
    final static String POSITIVES_DIR = "test/marmoset/a2/positive/";
    final static String NEGATIVES_DIR = "test/marmoset/a2/negative/";
    
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

        List<List<String>> positives = new ArrayList<List<String>>();
        int positives_passed = 0;
        List<List<String>> negatives = new ArrayList<List<String>>();
        int negatives_passed = 0;
        
        for (File f : new File(POSITIVES_DIR).listFiles()) {
        	positives.add(findTests(f));
        }
        
        for (File f : new File(NEGATIVES_DIR).listFiles()) {
        	negatives.add(findTests(f));
        }
        
        stdout.println("=== TESTING NEGATIVES ===");
        for (List<String> filenames : negatives) {
            stdout.printf("%-60s", "-> " + filenames.toString() + ":");
        	int retval = Compiler.compile(LR1_FILE, filenames.toArray(new String[0]));
        	if (retval == 42) {
        		stdout.println("+++ PASS +++");
                negatives_passed++;
        	} else {
        		stderr.println("--- FAIL ---");
            }
        }
        stdout.println("=== Passed " + negatives_passed + " / " + negatives.size() + " tests ===");
        
        stdout.println("\n=== TESTING POSITIVES ===");
        for (List<String> filenames : positives) {
            stdout.printf("%-60s", "-> " + filenames.toString() + ":");
            int retval = Compiler.compile(LR1_FILE, filenames.toArray(new String[0]));
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
    
    private static List<String> findTests(File f) {
    	List<String> tests = new ArrayList<String>();
    	if (f.getName().endsWith(".java")) {
            tests.add(f.getAbsolutePath());
    	} else if (f.isDirectory()) {
    		for (File child : f.listFiles()) {
    			tests.addAll(findTests(child));
    		}
    	}
    	return tests;
    }
}
