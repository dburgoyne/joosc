package AbstractSyntax;

import AbstractSyntax.UnaryExpression.UnaryOperator;
import Parser.ParseTree;
import Types.NullType;
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
				 : NullType.INSTANCE; 
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
	public void linkNames(TypeDecl curType, boolean staticCtx, EnvironmentDecl curDecl, Local curLocal, boolean lValue) {
		// Do nothing.
	}

	@Override public void checkTypes() throws TypeCheckingException {
		this.exprType = this.type.getType();
	}
	
	@Override
	public boolean isAlwaysTrue() {
		if (this.type.equals(LiteralType.BOOLEAN)) {
			return value.equals("true");
		} else {
			return false;
		}
	}
	
	@Override
	public boolean isAlwaysFalse() {
		if (this.type.equals(LiteralType.BOOLEAN)) {
			return value.equals("false");
		} else {
			return false;
		}
	}
}
