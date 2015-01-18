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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
			"/", "&", "|", "^", "%", "<<", ">>", ">>>", "+=", "-=", "*=", "/=",
			"&=", "|=", "^=", "%=", "<<=", ">>=", ">>>=" };

	public final static Regex[] OPERATOR_REGEXES;
	static {
		OPERATOR_REGEXES = new Regex[OPERATORS.length];
		for (int i = 0; i < OPERATORS.length; i++) {
			OPERATOR_REGEXES[i] = new Regex(OPERATORS[i]);
		}
	}

	public final static TokenType[] tokensToScanFor = {
			TokenType.IntegerLiteral, TokenType.FloatingPointLiteral,
			TokenType.BooleanLiteral, TokenType.CharacterLiteral,
			TokenType.StringLiteral, TokenType.NullLiteral,
			TokenType.Separator, TokenType.Identifier, TokenType.Keyword,
			TokenType.Operator, TokenType.WhiteSpace, TokenType.Comment, };

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

		List<Token> tokens = new ArrayList<Token>();
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
						.println("ERROR: Invalid lexical input element at line "
								+ line + ", column " + column);
				return null;
			}
			String lexeme = joosSource.substring(0, longestMatchLength);
			List<TokenType> largestList = new ArrayList<TokenType>();
			for (Entry<TokenType, Integer> entry : matchLengths.entrySet()) {
				if (entry.getValue() == longestMatchLength) {
					largestList.add(entry.getKey());
				}
			}
			// If Identifier and Keyword both matched, choose Keyword.
			if (largestList.contains(TokenType.Identifier)
					&& largestList.contains(TokenType.Keyword)) {
				largestList.remove(TokenType.Identifier);
			}

			if (largestList.size() != 1) {
				System.err
						.println("ERROR: Don't know how to consume " + lexeme);
				return null;
			}
			TokenType tokenType = largestList.get(0);
			// If the longest match was unique, we know how to consume it.
			System.out.println("Consuming \"" + lexeme + "\" as "
					+ tokenType.name());
			// TODO Store the lexeme and token type in a Token object, and
			// add it to a list.
			tokens.add(new Token(lexeme, tokenType, line, column));

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
			Path path = Paths.get(filename);
			String joosSource = "";
			try {
				// Read the entire source file into a string.
				joosSource = new String(Files.readAllBytes(path),
						StandardCharsets.UTF_8);
			} catch (IOException e) {
				System.err.println("Could not open the file " + filename);
				e.printStackTrace();
				return;
			}

			List<Token> tokens = scan(joosSource);

			for (Token token : tokens) {
				System.out.println(token);
			}
		}
	}
}
