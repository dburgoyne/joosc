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
}
