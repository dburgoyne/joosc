/*
 * CS 444
 * Assignment 1
 * 2015-01-18
 *
 * StringUtils.java
 *   String utility functions.
 *
 * AUTHORS:
 *   Danny Burgoyne UWID# 20411624 <secure@dburgoyne.ca>
 *   TODO add other contributors
 *   
 */

package Utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class StringUtils {

    // Counts the number of newlines in a string.
    public static int countNewlines(String s) {
        int newlines = 0;
        char previous = '\0';
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\r' || (c == '\n' && previous != '\r')) {
                newlines++;
            }
            previous = c;
        }
        return newlines;
    }
    
    // Counts the number of tabs in a string.
    public static int countTabs(String s) {
        int tabs = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\t') {
            	tabs++;
            }
        }
        return tabs;
    }
    
    public static String readFile(String path) throws FileNotFoundException {
    	Scanner s = null;
    	try {
    		s = new Scanner(new File(path), "UTF8");
    		return s.useDelimiter("\\Z").next();
    	} finally {
    		if (s != null) s.close();
    	}
    }
    
    // Extracts the filename from an absolute or relative path.
	public static String extractFileName( String path ) {
	    if ( path == null ) {
		    return null;
	    }
	
	    int slashPos = path.lastIndexOf( '\\' );
	    if ( slashPos == -1 ) {
		    slashPos = path.lastIndexOf( '/' );
	    }
	
	    return path.substring( slashPos > 0 ? slashPos + 1 : 0 );
	}
	
	// Validates an integer literal value.
	public static boolean validateIntegerLiteral(String lexeme,
			boolean isNegative) {
		if (isNegative) lexeme = "-" + lexeme;
		try {
			Integer.parseInt(lexeme);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
