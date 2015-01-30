/*
 * CS 444
 * Assignment 1
 * 2015-01-17
 *
 * CharacterClass.java
 *   Enumeration that implements the concept of character equivalence classes
 *   for use in regular expressions.
 *
 * AUTHORS:
 *   Danny Burgoyne UWID# 20411624 <secure@dburgoyne.ca>
 *   TODO add other contributors
 *   
 */
 
package Scanner;

interface P { boolean test(char c); }

public enum CharacterClass {
    HexDigit                                (new P() { public boolean test(char c) { return "0123456789abcdefABCDEF".indexOf(c) != -1; }} ),
    InputCharacter                          (new P() { public boolean test(char c) { return "\r\n".indexOf(c) == -1; }} ),
    InputCharacterNotSingleQuoteNotSlash    (new P() { public boolean test(char c) { return "\r\n\'\\".indexOf(c) == -1; }} ),
    InputCharacterNotDoubleQuoteNotSlash    (new P() { public boolean test(char c) { return "\r\n\"\\".indexOf(c) == -1; }} ),
    InputCharacterNotStar                   (new P() { public boolean test(char c) { return "\r\n*".indexOf(c) == -1; }} ),
    InputCharacterNotStarNotSlash           (new P() { public boolean test(char c) { return "\r\n*/".indexOf(c) == -1; }} ),
    Sub                                     (new P() { public boolean test(char c) { return c == (char)0x1a; }}),
    WhiteSpace                              (new P() { public boolean test(char c) { return " \t\f\r\n".indexOf(c) != -1; }} ),
    JavaLetter                              (new P() { public boolean test(char c) { return Character.isJavaIdentifierStart(c); }} ),
    JavaLetterOrDigit                       (new P() { public boolean test(char c) { return Character.isJavaIdentifierPart(c); }} ),
    Digit                                   (new P() { public boolean test(char c) { return "0123456789".indexOf(c) != -1; }} ),
    NonZeroDigit                            (new P() { public boolean test(char c) { return "123456789".indexOf(c) != -1; }} ),
    OctalDigit                              (new P() { public boolean test(char c) { return "01234567".indexOf(c) != -1; }} ),
    ZeroToThree                             (new P() { public boolean test(char c) { return "0123".indexOf(c) != -1; }} ),
    Separator                               (new P() { public boolean test(char c) { return "(){}[];,.".indexOf(c) != -1; }} ),
    IntegerTypeSuffix                       (new P() { public boolean test(char c) { return "lL".indexOf(c) != -1; }} ),
    ExponentIndicator                       (new P() { public boolean test(char c) { return "eE".indexOf(c) != -1; }} ),
    Sign                                    (new P() { public boolean test(char c) { return "+-".indexOf(c) != -1; }} ),
    FloatTypeSuffix                         (new P() { public boolean test(char c) { return "fFdD".indexOf(c) != -1; }} );

    private P m_predicate;
    
    CharacterClass(P predicate) {
        m_predicate = predicate;
    }
    
    public boolean matches(Character c) {
        return m_predicate.test(c);
    }
}