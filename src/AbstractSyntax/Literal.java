package AbstractSyntax;

import Parser.ParseTree;
import Types.PrimitiveType;
import Types.Type;
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
		
		public Type getType() {
			return (this == INTEGER) ? PrimitiveType.INT
				 : (this == BOOLEAN) ? PrimitiveType.BOOLEAN
				 : (this == CHARACTER) ? PrimitiveType.CHAR
				 : (this == STRING) ? Program.javaLangString
				 : null;  // TODO Since a type can be null, we need to null-check in a lot of places now!
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

	@Override
	public void linkNames(TypeDecl curType, boolean staticCtx) {
		// Do nothing.
	}

	@Override public void checkTypes() throws TypeCheckingException {
		this.exprType = this.type.getType();
	}
}
