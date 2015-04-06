package Compiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Assembler {
	
	final static String LIBRARY_DIR = "lib/5.0/";
    final static String OUTPUT_DIR = "output/";
    final static String ASSEMBLER_CMD = "/u/cs444/bin/nasm -O1 -f elf -g -F dwarf %s";
    final static String LINKER_CMD = "ld -melf_i386 -o " + OUTPUT_DIR + "main %s";
    final static String RUN_CMD = OUTPUT_DIR + "main";

    public static int assemble() throws InterruptedException, IOException {
    	Runtime.getRuntime().exec(
				String.format("cp %s/runtime.s " + OUTPUT_DIR, LIBRARY_DIR)).waitFor();
    	boolean success = true;

		// Attempt assembly
		for (String filename : listAssemblyFiles()) {
			Process p = Runtime.getRuntime().exec(String.format(ASSEMBLER_CMD, filename));
			int retval = p.waitFor();
			if (retval != 0) {
				Utilities.ProcessUtils.drainProcess(p, System.err);
				success = false;
			}
		}
		if (!success) {
			System.err.println("--- ASSEMBLY FAILED ---");
			return -1;
		}

		// Attempt linking
		Process p = Runtime.getRuntime().exec(String.format(LINKER_CMD, Utilities.StringUtils.join(listObjectFiles(), " ")));
		int retval = p.waitFor();
		if (retval != 0) {
			Utilities.ProcessUtils.drainProcess(p, System.err);
			System.err.println("--- LINKING FAILED ---");
			return retval;
		}
		/*		
		// Attempt to run the program
                class Drain extends Thread {
                    private java.io.InputStream is;
                    public java.io.StringWriter sw = new java.io.StringWriter();
                    private java.io.PrintWriter pw = new java.io.PrintWriter(sw);
                    Drain(java.io.InputStream is) {
                        this.is = is;
                    }
                    public void run() {
                        try {
                            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(is));
                            String line;
                            while ((line = br.readLine()) != null)
                                pw.println(line);
                        } catch (java.io.IOException ioe) {
                            ioe.printStackTrace();  
                        }
                    }
                }
                
                class Worker extends Thread {
                    public Integer retval;
                    public Drain out, err;
                    public void run() {
                        Process p = null;
                        
                        try {
                            p = Runtime.getRuntime().exec(RUN_CMD);
                            out = new Drain(p.getInputStream());
                            err = new Drain(p.getErrorStream());
                            out.start();
                            err.start();
                            retval = p.waitFor();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            if (p != null)
                                p.destroy();
                            if (out != null)
                                out.join();
                            if (err != null)
                                err.join();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                
                Worker w = new Worker();
                w.start();
                w.join(2000);

		if (w.retval == null || w.retval != 123) {
			System.err.println("--- EXECUTION FAILED ---");
			return retval;
                        }*/
		
		//cleanOutputDirectory();
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
