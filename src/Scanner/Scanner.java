/*
 * CS 444
 * Assignment 1
 * 2015-01-16
 *
 * Scanner.java
 *   Class that implements a regex-based scanner.
 *
 * AUTHORS:
 *   Danny Burgoyne UWID# 20411624 <secure@dburgoyne.ca>
 *   TODO add other contributors
 *   
 */
 
// TODO package this

public class Scanner {

    public static int longestPrefix(Regex r, String s) {
        int index = -1;
        for (int i = 0; i < s.length(); i++) {
            // TODO Benchmark this function with and without simplification here.
            r = Regex.simplify(Regex.derivative(r, s.charAt(i)));
            // If r is equivalent to the empty set, we can break from the loop.
            if (r.isEmptySet()) {
                break;
            }
            if (Regex.isNullable(r)) {
                index = i;
            }
        }
        return index;
    }
    
    public static void main(String[] args) {
    
        Regex r1 = new Regex(Regex.Type.DISJUNCTION,
                            new Regex("fizz"),
                            new Regex(Regex.Type.DISJUNCTION,
                                      new Regex("buzz"),
                                      new Regex("fizzbuzz")));
        Regex r2 = new Regex('f');
        String s = "fizzbuzzy buzzy fizzes";
        System.out.println(longestPrefix(r1, s));
        System.out.println(longestPrefix(r2, s));
    }
}

