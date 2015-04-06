package Utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class ProcessUtils {
	public static void drainProcess(Process p, PrintStream w) throws IOException {
		BufferedReader stdInput = new BufferedReader(new
             InputStreamReader(p.getInputStream()));

        BufferedReader stdError = new BufferedReader(new
             InputStreamReader(p.getErrorStream()));

        // read the output from the command
        String s;
        while ((s = stdInput.readLine()) != null) {
            w.println(s);
        }
         
        // read any errors from the attempted command
        while ((s = stdError.readLine()) != null) {
            w.println(s);
        }
    }
}
