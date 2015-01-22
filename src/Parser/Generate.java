package Parser;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

// Argument 1: .cfg file input
// Argument 2: .lr1 file output
public class Generate{
	public static final void main(String[] args){
        try{
			System.setIn(new FileInputStream(args[0]));
			System.setOut(new PrintStream(new FileOutputStream(args[1])));
		}catch(Exception e){
			e.printStackTrace();
			return;
		}
        Grammar grammar;
        try{
            grammar=Util.readGrammar(new Scanner(System.in));
            Util.writeGrammar(grammar);
        }catch(Error e){
        	e.printStackTrace();
			return;
        }
        Generator jlalr=new Generator(grammar);
        try{
            jlalr.computeFirstFollowNullable();
            jlalr.generateLALR1Table();
            jlalr.generateOutput();
        }catch(Error e){
        	e.printStackTrace();
			return;
        } 
    }
}
