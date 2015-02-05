package Parser;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

public class RunParserTests {

    final static String LR1_FILE = "joosc/src/Parser/joos1w.lr1"; 
    final static String POSITIVES_DIR = "joosc/test/parser/positive/";
    final static String NEGATIVES_DIR = "joosc/test/parser/negative/";
    
    public static void main(String[] args) {
        PrintStream stdout = System.out;
        System.setOut(new PrintStream(new java.io.OutputStream() {
            public void write(int b) {
                // Disable Tests' output
            }
        }));

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
            try {
                Parser.main(new String[] { LR1_FILE, NEGATIVES_DIR + filename});
                System.err.println("--- FAIL ---");
            } catch (Exception e) {
                stdout.println("+++ PASS +++");
                negatives_passed++;
            }
        }
        stdout.println("=== Passed " + negatives_passed + " / " + negatives.size() + " tests ===");
        
        stdout.println("\n=== TESTING POSITIVES ===");
        for (String filename : positives) {
            stdout.printf("%-60s", "-> " + filename + ":");
            try {
                Parser.main(new String[] { LR1_FILE, POSITIVES_DIR + filename});
                stdout.println("+++ PASS +++");
                positives_passed++;
            } catch (Exception e) {
                System.err.println("--- FAIL ---");
            }
        }
        
        stdout.println("=== Passed " + positives_passed + " / " + positives.size() + " tests ===");
    }
    
    
}
