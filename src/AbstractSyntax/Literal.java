package AbstractSyntax;

import CodeGeneration.AsmWriter;
import CodeGeneration.Frame;
import Exceptions.CodeGenerationException;
import Exceptions.TypeCheckingException;
import Exceptions.TypeLinkingException;
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
	protected String label;  // For String literals
	
	public Literal(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("Literal"));
		
		ParseTree firstChild = tree.getChildren()[0];
		assert(firstChild.isTerminal());
		this.type = LiteralType.fromString(firstChild.getSymbol());
		this.value = firstChild.getToken().getLexeme();
		
		// Remember all string literals in the program.
		if (this.type == LiteralType.STRING) {
			Program.allStringLiterals.add(this);
			this.label = "strlit_" + Program.allStringLiterals.size();
		}
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
	
	@Override public Object asConstExpr() {
		String escapedValue = this.value
			.replace("\\t", "\t")
			.replace("\\n", "\n")
			.replace("\\f", "\f")
			.replace("\\r", "\r")
			.replace("\\\"", "\"")
			.replace("\\\'", "\'")
			.replace("\\\\", "\\");
		switch (this.type) {
		case INTEGER:
			return Integer.parseInt(this.value);
		case BOOLEAN:
			return this.value.equals("true") ? Boolean.TRUE : Boolean.FALSE;
		case CHARACTER:
			assert this.value.startsWith("\'");
			assert this.value.endsWith("\'");
			return escapedValue.charAt(1);
		case STRING:
			assert this.value.startsWith("\"");
			assert this.value.endsWith("\"");
			return escapedValue.substring(1, escapedValue.length() - 1);
		default:
			return null;
		}
	}
	
	// ---------- Code generation ----------
	
	@Override public void generateCode(AsmWriter writer, Frame frame) throws CodeGenerationException {
		switch (this.type) {
		  case INTEGER:
			writer.instr("mov", "eax", this.asConstExpr());
			break;
		  case BOOLEAN:
			writer.instr("mov", "eax", (Boolean)(this.asConstExpr()) ? 1 : 0);
			break;
		  case CHARACTER:
			writer.instr("mov", "eax", (int)(((Character)(this.asConstExpr())).charValue()));
			break;
		  case STRING:
			writer.instr("mov", "eax", this.label);
			writer.justUsedGlobal(this.label);
			break;
		  case NULL:
			writer.instr("mov", "eax", 0);
			break;
		}
	}
}
