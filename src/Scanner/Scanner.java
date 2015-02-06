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

package Scanner;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import Utilities.StringUtils;

public class Scanner {

	// TODO These constants should likely go somewhere else.
	public final static int TAB_SIZE = 4;
	public final static String[] KEYWORDS = { "abstract", "default", "if",
			"private", "this", "boolean", "do", "implements", "protected",
			"throw", "break", "double", "import", "public", "throws", "byte",
			"else", "instanceof", "return", "transient", "case", "extends",
			"int", "short", "try", "catch", "final", "interface", "static",
			"void", "char", "finally", "long", "strictfp", "volatile", "class",
			"float", "native", "super", "while", "const", "for", "new",
			"switch", "continue", "goto", "package", "synchronized" };

	public final static Regex[] KEYWORD_REGEXES;
	static {
		KEYWORD_REGEXES = new Regex[KEYWORDS.length];
		for (int i = 0; i < KEYWORDS.length; i++) {
			KEYWORD_REGEXES[i] = new Regex(KEYWORDS[i]);
		}
	}

	public final static String[] OPERATORS = { "=", ">", "<", "!", "~", "?",
			":", "==", "<=", ">=", "!=", "&&", "||", "++", "--", "+", "-", "*",
			"/", "&", "|", "^", "%" };

	public final static Regex[] OPERATOR_REGEXES;
	static {
		OPERATOR_REGEXES = new Regex[OPERATORS.length];
		for (int i = 0; i < OPERATORS.length; i++) {
			OPERATOR_REGEXES[i] = new Regex(OPERATORS[i]);
		}
	}

	public final static TokenType[] tokensToScanFor = {
			TokenType.IntegerLiteral, TokenType.BooleanLiteral,
			TokenType.CharacterLiteral, TokenType.StringLiteral,
			TokenType.NullLiteral, TokenType.Separator, TokenType.Identifier,
			TokenType.Keyword, TokenType.Operator, TokenType.WhiteSpace,
			TokenType.Comment, TokenType.JavadocComment };

	// Returns the length of the longest prefix of s that matches r. Examples:
	// longestPrefix("ab*", "abbbbderp") = 5
	// longestPrefix("ab*", "cbbbbderp") = 0
	public static int longestPrefix(Regex r, String s) {
		int length = 0;
		for (int i = 0; i < s.length(); i++) {
			// TODO Benchmark this function with and without simplification
			// here.
			r = Regex.simplify(Regex.derivative(r, s.charAt(i)));
			// If r is equivalent to the empty set, we can break from the loop.
			if (r.isEmptySet()) {
				break;
			}
			if (Regex.isNullable(r)) {
				length = i + 1;
			}
		}
		return length;
	}
	
	public static List<Token> scan(String joosSource) {
	    return scan("<unspecified file>", joosSource);
	}

	public static List<Token> scan(String joosSourceFileName, String joosSource) {

		List<Token> tokens = new LinkedList<Token>();
		int line = 1, column = 1;

		while (joosSource.length() > 0) {
			// Compute the length of the longest prefix of the remaining
			// input matched by each token type regex.
			// TODO Consider trying to parallelize this.
			Map<TokenType, Integer> matchLengths = new HashMap<TokenType, Integer>();
			for (TokenType tokenType : tokensToScanFor) {
				matchLengths.put(tokenType,
						longestPrefix(tokenType.getRegex(), joosSource));
			}

			// Find the longest match(es).
			int longestMatchLength = Collections.max(matchLengths.values());
			if (longestMatchLength == 0) {
				System.err
						.println("ERROR: Invalid lexical input element at "
						        + "file " + joosSourceFileName + ", line "
								+ line + ", column " + column);
				return null;
			}
			String lexeme = joosSource.substring(0, longestMatchLength);
			List<TokenType> largestList = new LinkedList<TokenType>();
			for (Entry<TokenType, Integer> entry : matchLengths.entrySet()) {
				if (entry.getValue() == longestMatchLength) {
					largestList.add(entry.getKey());
				}
			}
			// If Identifier and Keyword/NullLiteral/BooleanLiteral both matched, choose the latter.
			if (largestList.contains(TokenType.Identifier)
					&& (largestList.contains(TokenType.Keyword) ||
			            largestList.contains(TokenType.NullLiteral) ||
                        largestList.contains(TokenType.BooleanLiteral))) {
				largestList.remove(TokenType.Identifier);
			}

			if (largestList.size() != 1) {
				System.err.printf("ERROR: Don't know how to consume lexeme '%s'" + 
				                "\n in file %s, line %d, column %d\n", 
				                  lexeme, joosSourceFileName, line, column);
				return null;
			}
			TokenType tokenType = largestList.get(0);
			// If the longest match was unique, we know how to consume it.
			if (tokenType != TokenType.WhiteSpace
			        && tokenType != TokenType.Comment
			        && tokenType != TokenType.JavadocComment) {
				System.out.println("Consuming \"" + lexeme + "\" as "
						+ tokenType.name());
				tokens.add(new Token(lexeme, tokenType, joosSourceFileName, line, column));
			}

			// Calculate the new line and column number.
			int numberOfNewlines = StringUtils.countNewlines(lexeme);
			int indexOfLastNewline = Math.max(lexeme.lastIndexOf("\r"),
					lexeme.lastIndexOf("\n"));
			// TODO This counts tabs as a single column.
			line += numberOfNewlines;
			column = (numberOfNewlines == 0) ? column + longestMatchLength
					: longestMatchLength - indexOfLastNewline;

			// Strip the token off the input and continue.
			joosSource = joosSource.substring(longestMatchLength);
		}

		return tokens;
	}

	public static void main(String[] args) {

		if (args.length > 0) {
			String filename = args[0];
			String joosSource = "";
			try {
				// Read the entire source file into a string.
				joosSource = StringUtils.readFile(filename);
			} catch (IOException e) {
				System.err.println("Could not open the file " + filename);
				e.printStackTrace();
				return;
			}

			List<Token> tokens = scan(filename, joosSource);

			for (Token token : tokens) {
				System.out.println(token);
			}
		}
	}
}
