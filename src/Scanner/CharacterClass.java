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
 
// TODO package this

import java.util.function.Predicate;

public enum CharacterClass {
    HexDigit                                (c -> "0123456789abcdefABCDEF".indexOf(c) != -1 ),
    InputCharacter                          (c -> "\r\n".indexOf(c) == -1 ),
    InputCharacterNotSingleQuoteNotSlash    (c -> "\r\n\'\\".indexOf(c) == -1 ),
    InputCharacterNotDoubleQuoteNotSlash    (c -> "\r\n\"\\".indexOf(c) == -1 ),
    InputCharacterNotStar                   (c -> "\r\n*".indexOf(c) == -1 ),
    InputCharacterNotStarNotSlash           (c -> "\r\n*/".indexOf(c) == -1 ),
    Sub                                     (c -> c == (char)0x1a),
    WhiteSpace                              (c -> " \t\f\r\n".indexOf(c) != -1 ),
    JavaLetter                              (c -> Character.isJavaIdentifierStart(c) ),
    JavaLetterOrDigit                       (c -> Character.isJavaIdentifierPart(c) ),
    Digit                                   (c -> "0123456789".indexOf(c) != -1 ),
    NonZeroDigit                            (c -> "123456789".indexOf(c) != -1 ),
    OctalDigit                              (c -> "01234567".indexOf(c) != -1 ),
    ZeroToThree                             (c -> "0123".indexOf(c) != -1 ),
    Separator                               (c -> "(){}[];,.".indexOf(c) != -1 ),
    IntegerTypeSuffix                       (c -> "lL".indexOf(c) != -1 ),
    ExponentIndicator                       (c -> "eE".indexOf(c) != -1 ),
    Sign                                    (c -> "+-".indexOf(c) != -1 ),
    FloatTypeSuffix                         (c -> "fFdD".indexOf(c) != -1 );

    private Predicate<Character> m_predicate;
    
    CharacterClass(Predicate<Character> predicate) {
        m_predicate = predicate;
    }
    
    public boolean matches(Character c) {
        return m_predicate.test(c);
    }
}