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
    private String lexeme, fileName;
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
    
    public String getFileName() {
        return fileName;
    }

    public Token(String lexeme, TokenType tokenType, String fileName, int line, int column) {
        this.lexeme = lexeme;
        this.tokenType = tokenType;
        this.fileName = fileName;
        this.line = line;
        this.column = column;
    }

    public String toString() {
        return tokenType.name() + " (" + lexeme + ") at "
                + fileName + " line " + line + " column " + column;
    }
    
    /// Returns the .cfg file non-terminal symbol matching this token.
    ///  e.g. identifiers appear as Identifier, but keywords and separators
    ///       appear as their lexeme.
    public String getCfgName() {
        switch (tokenType) {
        case Keyword:
        case Operator:
        case Separator:
            return lexeme.trim();            
        default:
            return tokenType.name();
        }
    }
    
}