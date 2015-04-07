package Compiler;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class RunA5CompilerTests {

    final static String LR1_FILE = "src/Parser/joos1w.lr1"; 
    final static String POSITIVES_DIR = "test/marmoset/a5/positive/";
    final static String NEGATIVES_DIR = "test/marmoset/a5/negative/";
    final static String LIBRARY_DIR = "lib/5.0/";
    final static String OUTPUT_DIR = "output/";
    final static String ASSEMBLER_CMD = "/u/cs444/bin/nasm -O1 -f elf -g -F dwarf %s";
    final static String LINKER_CMD = "ld -melf_i386 -o " + OUTPUT_DIR + "/main %s";
    
    public static void main(String[] args) throws IOException, InterruptedException {
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
        List<String> libraries = new ArrayList<String>();
        
        for (File f : new File(POSITIVES_DIR).listFiles()) {
        	positives.add(findTests(f));
        }
        
        for (File f : new File(NEGATIVES_DIR).listFiles()) {
        	negatives.add(findTests(f));
        }
        
        for (File f : new File(LIBRARY_DIR).listFiles()) {
        	libraries.addAll(findTests(f));
        }
        stdout.println("=== TESTING NEGATIVES ===");
        fail_outer: for (List<String> filenames : negatives) {
        	// Clean output directory.
        	Assembler.cleanOutputDirectory();
            stdout.printf("%-60s", "-> " + filenames.toString() + ":\n");
            // Include all standard libraries in the build.
            filenames.addAll(libraries);
        	int retval = Compiler.compile(LR1_FILE, filenames.toArray(new String[0]));
        	if (retval == 0) {
    			// Attempt assembly and linking.
        		retval = Assembler.assemble(stderr);
    			if (retval != 0) {
    				continue fail_outer;
    			}
        		retval = Runner.run(stderr);
        		if (retval == 123) {
        			stdout.println("--- FAIL ---");
    				continue fail_outer;
    			}
    			stdout.println("+++ PASS +++");
                negatives_passed++;
        	} else {
        		stderr.println("--- COMPILATION FAILED ---");
            }
        }
        stdout.println("=== Passed " + negatives_passed + " / " + negatives.size() + " tests ===");
        
        stdout.println("\n=== TESTING POSITIVES ===");
        pass_outer: for (List<String> filenames : positives) {
        	// Clean output directory.
        	Assembler.cleanOutputDirectory();
            stdout.printf("%-60s", "-> " + filenames.toString() + ":\n");
            // Include all standard libraries in the build.
            filenames.addAll(libraries);
        	int retval = Compiler.compile(LR1_FILE, filenames.toArray(new String[0]));
        	if (retval == 0) {
        		// Attempt assembly and linking.
        		retval = Assembler.assemble(stderr);
    			if (retval != 0) {
    				continue pass_outer;
    			}
        		retval = Runner.run(stderr);
        		if (retval != 123) {
        			stdout.println("--- FAIL ---");
    				continue pass_outer;
    			}
    			stdout.println("+++ PASS +++");
                positives_passed++;
        	} else {
        		stderr.println("--- COMPILATION FAILED ---");
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
