package Compiler;

import java.io.IOException;
import java.io.PrintStream;

public class Runner {
	
    final static String RUN_CMD = Assembler.OUTPUT_DIR + "main";
    
    public static int run() throws InterruptedException, IOException {
    	return run(System.err);
    }

    public static int run(PrintStream stream) throws InterruptedException, IOException {
		// Attempt to run the program
		Process p = Runtime.getRuntime().exec(RUN_CMD);
		Utilities.ProcessUtils.drainProcess(p);
		int retval = p.waitFor();
		stream.println("Execution completed with exit status " + retval);
		return retval;
    }
}
