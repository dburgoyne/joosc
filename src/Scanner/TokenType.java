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
 *   Uros Dimitrijevic UWID# 20373732 <udimitri@uwaterloo.ca>
 *   Xiang Fang <x2fang@uwaterloo.ca>
 *   
 */

package Scanner;

public enum TokenType {
    
    // (EOF token not used in Scanner, but all Parser input ends with it) 
    EOF(Regex.Build(Regex.Type.EMPTY_SET)),
    
    // (Error token in case we want to catch parsing errors)
    // ERROR(Regex.Build(Regex.Type.EMPTY_SET)),
    
	LineTerminator(Regex.Build(Regex.Type.DISJUNCTION, new Regex('\r'),
			new Regex('\n'), new Regex("\r\n"))),

	WhiteSpace(Regex.Build(Regex.Type.CONCATENATION, new Regex(
			CharacterClass.WhiteSpace), new Regex(Regex.Type.KLEENE_CLOSURE,
			new Regex(CharacterClass.WhiteSpace)))),

	CharactersInLine(Regex.Build(Regex.Type.CONCATENATION, new Regex(
			CharacterClass.InputCharacter),
			new Regex(Regex.Type.KLEENE_CLOSURE, new Regex(
					CharacterClass.InputCharacter)))),

	EndOfLineComment(Regex.Build(Regex.Type.CONCATENATION, new Regex("//"),
			CharactersInLine.getRegex(), LineTerminator.getRegex())),

	NotStar(Regex.Build(Regex.Type.DISJUNCTION, new Regex(
			CharacterClass.InputCharacterNotStar), LineTerminator.getRegex())),

	NotStarNotSlash(Regex.Build(Regex.Type.DISJUNCTION, new Regex(
			CharacterClass.InputCharacterNotStarNotSlash), LineTerminator
			.getRegex())),

	// This is my attempt to resolve the mutually-recursive productions
	// CommentTail and
	// CommentTailStar. It is based on a regex found at
	// http://www.cs.dartmouth.edu/~mckeeman/cs118/assignments/comment.html
	// TO DO This regex should be thoroughly tested.

	TraditionalComment(

	Regex.Build(Regex.Type.DISJUNCTION, new Regex("/**/"), Regex.Build(
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
			new Regex("/")))),

	JavadocComment(Regex
			.Build(Regex.Type.DISJUNCTION, new Regex("/***/"),
					Regex.Build(
							Regex.Type.CONCATENATION,
							new Regex("/**"),
							NotStarNotSlash.getRegex(),
							new Regex(Regex.Type.KLEENE_CLOSURE, Regex.Build(
									Regex.Type.DISJUNCTION, new Regex('/'),
									new Regex(Regex.Type.CONCATENATION,
											new Regex(
													Regex.Type.KLEENE_CLOSURE,
													new Regex('*')),
											NotStarNotSlash.getRegex()))),
							new Regex(Regex.Type.CONCATENATION, new Regex('*'),
									new Regex(Regex.Type.KLEENE_CLOSURE,
											new Regex('*'))), new Regex("/")))),

	Comment(Regex.Build(Regex.Type.DISJUNCTION, TraditionalComment.getRegex(),
			EndOfLineComment.getRegex())),

	IdentifierChars(Regex.Build(Regex.Type.CONCATENATION, new Regex(
			CharacterClass.JavaLetter), new Regex(Regex.Type.KLEENE_CLOSURE,
			new Regex(CharacterClass.JavaLetterOrDigit)))),
	// TO DO It will be necessary to identify keywords explicitly in the scanner
	// and invalidate illegitimate matches of this regex.
	Identifier(IdentifierChars.getRegex()),

	Keyword(Regex.Build(Regex.Type.DISJUNCTION, Scanner.KEYWORD_REGEXES)),

	Digits(Regex
			.Build(Regex.Type.CONCATENATION, new Regex(CharacterClass.Digit),
					new Regex(Regex.Type.KLEENE_CLOSURE, new Regex(
							CharacterClass.Digit)))),

	DecimalNumeral(Regex.Build(Regex.Type.DISJUNCTION, new Regex('0'), Regex
			.Build(Regex.Type.CONCATENATION, new Regex(
					CharacterClass.NonZeroDigit), Regex.Optional(Digits
					.getRegex())))),

	SignedIntegerLiteral(Regex.Build(Regex.Type.CONCATENATION, new Regex(
			CharacterClass.Sign), Digits.getRegex())),
	
	IntegerLiteral(DecimalNumeral.getRegex()),
	//IntegerLiteral(Regex.Build(Regex.Type.DISJUNCTION, 
	//		DecimalNumeral.getRegex(),
	//		SignedIntegerLiteral.getRegex())),

	ExponentPart(Regex.Build(Regex.Type.CONCATENATION, new Regex(
			CharacterClass.ExponentIndicator), SignedIntegerLiteral.getRegex())),

	BooleanLiteral(Regex.Build(Regex.Type.DISJUNCTION, new Regex("true"),
			new Regex("false"))),

	// TO DO We may not need these.
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

	Operator(Regex.Build(Regex.Type.DISJUNCTION, Scanner.OPERATOR_REGEXES)),

	Separator(new Regex(CharacterClass.Separator));

	private Regex m_regex;

	public Regex getRegex() {
		return m_regex;
	}

	TokenType(Regex regex) {
		m_regex = regex;
	}
}