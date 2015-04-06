package Compiler;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Assembler {
	
	final static String LIBRARY_DIR = "lib/5.0/";
    final static String OUTPUT_DIR = "output/";
    final static String ASSEMBLER_CMD = "/u/cs444/bin/nasm -O1 -f elf -g -F dwarf %s";
    final static String LINKER_CMD = "ld -melf_i386 -o " + OUTPUT_DIR + "main %s";
    final static String RUN_CMD = OUTPUT_DIR + "main";
    
    public static int assemble() throws InterruptedException, IOException {
    	return assemble(System.err);
    }

    public static int assemble(PrintStream stream) throws InterruptedException, IOException {
    	Runtime.getRuntime().exec(
				String.format("cp %s/runtime.s " + OUTPUT_DIR, LIBRARY_DIR)).waitFor();
    	boolean success = true;

		// Attempt assembly
		for (String filename : listAssemblyFiles()) {
			Process p = Runtime.getRuntime().exec(String.format(ASSEMBLER_CMD, filename));
			int retval = p.waitFor();
			if (retval != 0) {
				Utilities.ProcessUtils.drainProcess(p);
				success = false;
			}
		}
		if (!success) {
			stream.println("--- ASSEMBLY FAILED ---");
			return -1;
		}

		// Attempt linking
		Process p = Runtime.getRuntime().exec(String.format(LINKER_CMD, Utilities.StringUtils.join(listObjectFiles(), " ")));
		int retval = p.waitFor();
		if (retval != 0) {
			Utilities.ProcessUtils.drainProcess(p);
			stream.println("--- LINKING FAILED ---");
			return retval;
		}

		return 0;
    }
    
    public static void cleanOutputDirectory() {
    	for(File file: new File(OUTPUT_DIR).listFiles()) {
    		file.delete();
    	}
    }
	
	public static List<String> listAssemblyFiles() {
    	File dir = new File(OUTPUT_DIR);
    	List<String> sfiles = new ArrayList<String>();
    	for (File child : dir.listFiles()) {
    		if (child.getName().endsWith(".s")) {
                sfiles.add(OUTPUT_DIR + child.getName());
        	}
    	}
    	return sfiles;
    }
	
	public static List<String> listObjectFiles() {
    	File dir = new File(OUTPUT_DIR);
    	List<String> sfiles = new ArrayList<String>();
    	for (File child : dir.listFiles()) {
    		if (child.getName().endsWith(".o")) {
                sfiles.add(OUTPUT_DIR + child.getName());
        	}
    	}
    	return sfiles;
    }
}
