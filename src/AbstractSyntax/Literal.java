package AbstractSyntax;

import Parser.ParseTree;
import Utilities.Cons;

public class Literal extends Expression {
	
	enum LiteralType {
		INTEGER,
		BOOLEAN,
		CHARACTER,
		STRING,
		NULL;
		
		public static LiteralType fromString(String s) {
			return s.equals("IntegerLiteral") ? INTEGER
				 : s.equals("BooleanLiteral") ? BOOLEAN
				 : s.equals("CharacterLiteral") ? CHARACTER
				 : s.equals("StringLiteral") ? STRING
				 : s.equals("NullLiteral") ? NULL
				 : null;
		}
	};
	
	protected LiteralType type;
	protected String value;
	
	public Literal(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("Literal"));
		
		ParseTree firstChild = tree.getChildren()[0];
		assert(firstChild.isTerminal());
		this.type = LiteralType.fromString(firstChild.getSymbol());
		this.value = firstChild.getToken().getLexeme();
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) {
		this.environment = parentEnvironment;
	}

	@Override
	public void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException {
		// Do nothing.
	}
}
