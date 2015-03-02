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

import Utilities.Predicate;

public enum CharacterClass {
    HexDigit                                (new Predicate<Character>() { public boolean test(Character c) { return "0123456789abcdefABCDEF".indexOf(c) != -1; }} ),
    InputCharacter                          (new Predicate<Character>() { public boolean test(Character c) { return "\r\n".indexOf(c) == -1; }} ),
    InputCharacterNotSingleQuoteNotSlash    (new Predicate<Character>() { public boolean test(Character c) { return "\r\n\'\\".indexOf(c) == -1; }} ),
    InputCharacterNotDoubleQuoteNotSlash    (new Predicate<Character>() { public boolean test(Character c) { return "\r\n\"\\".indexOf(c) == -1; }} ),
    InputCharacterNotStar                   (new Predicate<Character>() { public boolean test(Character c) { return "\r\n*".indexOf(c) == -1; }} ),
    InputCharacterNotStarNotSlash           (new Predicate<Character>() { public boolean test(Character c) { return "\r\n*/".indexOf(c) == -1; }} ),
    Sub                                     (new Predicate<Character>() { public boolean test(Character c) { return c == (char)0x1a; }}),
    WhiteSpace                              (new Predicate<Character>() { public boolean test(Character c) { return " \t\f\r\n".indexOf(c) != -1; }} ),
    JavaLetter                              (new Predicate<Character>() { public boolean test(Character c) { return Character.isJavaIdentifierStart(c); }} ),
    JavaLetterOrDigit                       (new Predicate<Character>() { public boolean test(Character c) { return Character.isJavaIdentifierPart(c); }} ),
    Digit                                   (new Predicate<Character>() { public boolean test(Character c) { return "0123456789".indexOf(c) != -1; }} ),
    NonZeroDigit                            (new Predicate<Character>() { public boolean test(Character c) { return "123456789".indexOf(c) != -1; }} ),
    OctalDigit                              (new Predicate<Character>() { public boolean test(Character c) { return "01234567".indexOf(c) != -1; }} ),
    ZeroToThree                             (new Predicate<Character>() { public boolean test(Character c) { return "0123".indexOf(c) != -1; }} ),
    Separator                               (new Predicate<Character>() { public boolean test(Character c) { return "(){}[];,.".indexOf(c) != -1; }} ),
    IntegerTypeSuffix                       (new Predicate<Character>() { public boolean test(Character c) { return "lL".indexOf(c) != -1; }} ),
    ExponentIndicator                       (new Predicate<Character>() { public boolean test(Character c) { return "eE".indexOf(c) != -1; }} ),
    Sign                                    (new Predicate<Character>() { public boolean test(Character c) { return "+-".indexOf(c) != -1; }} ),
    FloatTypeSuffix                         (new Predicate<Character>() { public boolean test(Character c) { return "fFdD".indexOf(c) != -1; }} );

    private Predicate<Character> m_predicate;
    
    CharacterClass(Predicate<Character> predicate) {
        m_predicate = predicate;
    }
    
    public boolean matches(Character c) {
        return m_predicate.test(c);
    }
}
