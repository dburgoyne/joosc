package Utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class ProcessUtils {
	public static void drainProcess(Process p) throws IOException {
		inheritIO(p.getInputStream(), System.out);
	    inheritIO(p.getErrorStream(), System.err);
    }
	
	// From http://stackoverflow.com/questions/14165517/processbuilder-forwarding-stdout-and-stderr-of-started-processes-without-blocki
	private static void inheritIO(final InputStream src, final PrintStream dest) {
	    new Thread(new Runnable() {
	        public void run() {
	            Scanner sc = new Scanner(src);
	            while (sc.hasNextLine()) {
	                dest.println(sc.nextLine());
	            }
	        }
	    }).start();
	}
	
	/*
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
	 */
	
}
