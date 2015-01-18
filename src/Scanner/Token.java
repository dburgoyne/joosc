/*
 * CS 444
 * Assignment 1
 * 2015-01-18
 *
 * Token.java
 *   Class that implements the concept of a lexical token for use in scanning.
 *
 * AUTHORS:
 *   Danny Burgoyne UWID# 20411624 <secure@dburgoyne.ca>
 *   TODO add other contributors
 *   
 */

package Scanner;

public class Token {
    private String lexeme;
    private TokenType tokenType;
    private int line, column;

    public String getLexeme() {
        return lexeme;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public Token(String lexeme, TokenType tokenType, int line, int column) {
        this.lexeme = lexeme;
        this.tokenType = tokenType;
        this.line = line;
        this.column = column;
    }

    public String toString() {
        return tokenType.name() + " (" + lexeme + ") line " + line + " column "
                + column;
    }
}