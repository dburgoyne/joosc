/*
 * CS 444
 * Assignment 1
 * 2015-01-17
 *
 * TokenType.java
 *   Enumeration that implements the concept of token types for use in scanning.
 *
 * AUTHORS:
 *   Danny Burgoyne UWID# 20411624 <secure@dburgoyne.ca>
 *   TODO add other contributors
 *   
 */

package Scanner;

public enum TokenType {
    // TODO Not sure if we need to match Unicode escapes.
    UnicodeMarker(Regex.Build(Regex.Type.CONCATENATION, new Regex('u'),
            new Regex(Regex.Type.KLEENE_CLOSURE, new Regex('u')))), UnicodeEscape(
            Regex.Build(Regex.Type.CONCATENATION, new Regex('\\'),
                    UnicodeMarker.getRegex(),
                    new Regex(CharacterClass.HexDigit), new Regex(
                            CharacterClass.HexDigit), new Regex(
                            CharacterClass.HexDigit), new Regex(
                            CharacterClass.HexDigit))), LineTerminator(Regex
            .Build(Regex.Type.DISJUNCTION, new Regex('\r'), new Regex('\n'),
                    new Regex("\r\n"))),

    WhiteSpace(Regex.Build(Regex.Type.CONCATENATION, new Regex(
            CharacterClass.WhiteSpace), new Regex(Regex.Type.KLEENE_CLOSURE,
            new Regex(CharacterClass.WhiteSpace)))),

    CharactersInLine(Regex.Build(Regex.Type.CONCATENATION, new Regex(
            CharacterClass.InputCharacter),
            new Regex(Regex.Type.KLEENE_CLOSURE, new Regex(
                    CharacterClass.InputCharacter)))), EndOfLineComment(Regex
            .Build(Regex.Type.CONCATENATION, new Regex("//"),
                    CharactersInLine.getRegex(), LineTerminator.getRegex())),

    NotStar(Regex.Build(Regex.Type.DISJUNCTION, new Regex(
            CharacterClass.InputCharacterNotStar), LineTerminator.getRegex())), NotStarNotSlash(
            Regex.Build(Regex.Type.DISJUNCTION, new Regex(
                    CharacterClass.InputCharacterNotStarNotSlash),
                    LineTerminator.getRegex())),

    // This is my attempt to resolve the mutually-recursive productions
    // CommentTail and
    // CommentTailStar. It is based on a regex found at
    // http://www.cs.dartmouth.edu/~mckeeman/cs118/assignments/comment.html
    // TODO This regex should be thoroughly tested.

    TraditionalComment(Regex.Build(
            Regex.Type.CONCATENATION,
            new Regex("/*"),
            NotStar.getRegex(),
            new Regex(Regex.Type.KLEENE_CLOSURE, Regex.Build(
                    Regex.Type.DISJUNCTION, new Regex('/'), new Regex(
                            Regex.Type.CONCATENATION, new Regex(
                                    Regex.Type.KLEENE_CLOSURE, new Regex('*')),
                            NotStarNotSlash.getRegex()))), new Regex(
                    Regex.Type.CONCATENATION, new Regex('*'), new Regex(
                            Regex.Type.KLEENE_CLOSURE, new Regex('*'))),
            new Regex("/"))), Comment(Regex.Build(Regex.Type.DISJUNCTION,
            TraditionalComment.getRegex(), EndOfLineComment.getRegex())),

    IdentifierChars(Regex.Build(Regex.Type.CONCATENATION, new Regex(
            CharacterClass.JavaLetter), new Regex(Regex.Type.KLEENE_CLOSURE,
            new Regex(CharacterClass.JavaLetterOrDigit)))),
    // TODO It will be necessary to identify keywords explicitly in the scanner
    // and invalidate illegitimate matches of this regex.
    Identifier(IdentifierChars.getRegex()),
    // I really wish Java had a less verbose functional map.
    Keyword(Regex.Build(Regex.Type.DISJUNCTION, Scanner.KEYWORD_REGEXES)),

    Digits(Regex
            .Build(Regex.Type.CONCATENATION, new Regex(CharacterClass.Digit),
                    new Regex(Regex.Type.KLEENE_CLOSURE, new Regex(
                            CharacterClass.Digit)))),

    DecimalNumeral(Regex.Build(Regex.Type.DISJUNCTION, new Regex('0'), Regex
            .Build(Regex.Type.CONCATENATION, new Regex(
                    CharacterClass.NonZeroDigit), Regex.Optional(Digits
                    .getRegex())))),

    HexDigits(Regex.Build(Regex.Type.CONCATENATION, new Regex(
            CharacterClass.HexDigit), new Regex(Regex.Type.KLEENE_CLOSURE,
            new Regex(CharacterClass.HexDigit)))),

    HexNumeral(Regex.Build(Regex.Type.DISJUNCTION, Regex.Build(
            Regex.Type.CONCATENATION,
            new Regex("0x"),
            HexDigits.getRegex(),
            Regex.Build(Regex.Type.CONCATENATION, new Regex("0X"),
                    HexDigits.getRegex())))),

    OctalDigits(Regex.Build(Regex.Type.CONCATENATION, new Regex(
            CharacterClass.OctalDigit), new Regex(Regex.Type.KLEENE_CLOSURE,
            new Regex(CharacterClass.OctalDigit)))),

    OctalNumeral(Regex.Build(Regex.Type.CONCATENATION, new Regex('0'),
            OctalDigits.getRegex())),

    DecimalIntegerLiteral(Regex.Build(Regex.Type.CONCATENATION,
            DecimalNumeral.getRegex(),
            Regex.Optional(new Regex(CharacterClass.IntegerTypeSuffix)))),

    HexIntegerLiteral(Regex.Build(Regex.Type.CONCATENATION,
            HexNumeral.getRegex(),
            Regex.Optional(new Regex(CharacterClass.IntegerTypeSuffix)))),

    OctalIntegerLiteral(Regex.Build(Regex.Type.CONCATENATION,
            OctalNumeral.getRegex(),
            Regex.Optional(new Regex(CharacterClass.IntegerTypeSuffix)))),

    IntegerLiteral(Regex.Build(Regex.Type.DISJUNCTION,
            DecimalIntegerLiteral.getRegex(), HexIntegerLiteral.getRegex(),
            OctalIntegerLiteral.getRegex())),

    SignedInteger(Regex.Build(Regex.Type.CONCATENATION, new Regex(
            CharacterClass.Sign), Digits.getRegex())),

    ExponentPart(Regex.Build(Regex.Type.CONCATENATION, new Regex(
            CharacterClass.ExponentIndicator), SignedInteger.getRegex())),

    BooleanLiteral(Regex.Build(Regex.Type.DISJUNCTION, new Regex("true"),
            new Regex("false"))),

    FloatingPointLiteral(Regex.Build(Regex.Type.DISJUNCTION, Regex.Build(
            Regex.Type.CONCATENATION, Digits.getRegex(), new Regex('.'),
            Regex.Optional(Digits.getRegex()),
            Regex.Optional(ExponentPart.getRegex()),
            Regex.Optional(new Regex(CharacterClass.FloatTypeSuffix))), Regex
            .Build(Regex.Type.CONCATENATION, new Regex('.'), Digits.getRegex(),
                    Regex.Optional(ExponentPart.getRegex()),
                    Regex.Optional(new Regex(CharacterClass.FloatTypeSuffix))),
            Regex.Build(Regex.Type.CONCATENATION, Digits.getRegex(),
                    ExponentPart.getRegex(),
                    Regex.Optional(new Regex(CharacterClass.FloatTypeSuffix))),
            Regex.Build(Regex.Type.CONCATENATION, Digits.getRegex(), Regex
                    .Optional(ExponentPart.getRegex()), new Regex(
                    CharacterClass.FloatTypeSuffix)))),

    OctalEscape(Regex.Build(Regex.Type.DISJUNCTION, Regex.Build(
            Regex.Type.CONCATENATION, new Regex('\\'), new Regex(
                    CharacterClass.OctalDigit)), Regex.Build(
            Regex.Type.CONCATENATION, new Regex('\\'), new Regex(
                    CharacterClass.OctalDigit), new Regex(
                    CharacterClass.OctalDigit)), Regex.Build(
            Regex.Type.CONCATENATION, new Regex('\\'), new Regex(
                    CharacterClass.ZeroToThree), new Regex(
                    CharacterClass.OctalDigit), new Regex(
                    CharacterClass.OctalDigit)))),

    EscapeSequence(Regex.Build(Regex.Type.DISJUNCTION, new Regex("\\b"),
            new Regex("\\t"), new Regex("\\n"), new Regex("\\f"), new Regex(
                    "\\r"), new Regex("\\\""), new Regex("\\\'"), new Regex(
                    "\\\\"), OctalEscape.getRegex())),

    SingleCharacter(new Regex(
            CharacterClass.InputCharacterNotSingleQuoteNotSlash)),

    CharacterLiteral(Regex.Build(Regex.Type.CONCATENATION, new Regex('\''),
            Regex.Build(Regex.Type.DISJUNCTION, SingleCharacter.getRegex(),
                    EscapeSequence.getRegex()), new Regex('\''))),

    StringCharacter(Regex.Build(Regex.Type.DISJUNCTION, new Regex(
            CharacterClass.InputCharacterNotDoubleQuoteNotSlash),
            EscapeSequence.getRegex())),

    StringCharacters(Regex.Build(Regex.Type.CONCATENATION, StringCharacter
            .getRegex(),
            new Regex(Regex.Type.KLEENE_CLOSURE, StringCharacter.getRegex()))),

    StringLiteral(Regex.Build(Regex.Type.CONCATENATION, new Regex('\"'),
            Regex.Optional(StringCharacters.getRegex()), new Regex('\"'))),

    NullLiteral(new Regex("null")),

    Literal(Regex.Build(Regex.Type.DISJUNCTION, IntegerLiteral.getRegex(),
            FloatingPointLiteral.getRegex(), BooleanLiteral.getRegex(),
            CharacterLiteral.getRegex(), StringLiteral.getRegex(),
            NullLiteral.getRegex())),

    Operator(Regex.Build(Regex.Type.DISJUNCTION, Scanner.OPERATOR_REGEXES)),

    Separator(new Regex(CharacterClass.Separator)),

    Token(Regex.Build(Regex.Type.DISJUNCTION, Identifier.getRegex(),
            Keyword.getRegex(), Literal.getRegex(), Separator.getRegex(),
            Operator.getRegex())),

    InputElement(Regex.Build(Regex.Type.DISJUNCTION, WhiteSpace.getRegex(),
            Comment.getRegex(), Token.getRegex())),

    InputElements(Regex.Build(Regex.Type.CONCATENATION,
            InputElement.getRegex(), new Regex(Regex.Type.KLEENE_CLOSURE,
                    InputElement.getRegex()))),

    Input(Regex.Build(Regex.Type.CONCATENATION,
            Regex.Optional(InputElements.getRegex()),
            Regex.Optional(new Regex(CharacterClass.Sub))));

    private Regex m_regex;

    public Regex getRegex() {
        return m_regex;
    }

    TokenType(Regex regex) {
        m_regex = regex;
    }
}