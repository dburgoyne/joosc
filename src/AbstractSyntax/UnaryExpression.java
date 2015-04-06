package AbstractSyntax;

import CodeGeneration.AsmWriter;
import CodeGeneration.Frame;
import Exceptions.CodeGenerationException;
import Exceptions.ImportException;
import Exceptions.NameConflictException;
import Exceptions.NameLinkingException;
import Exceptions.TypeCheckingException;
import Exceptions.TypeLinkingException;
import Parser.ParseTree;
import Types.PrimitiveType;
import Types.Type;
import Utilities.Cons;

public class UnaryExpression extends Expression {

	enum UnaryOperator {
		NOT,
		MINUS;
		
		public static UnaryOperator fromString(String s) {
			return s.equals("!") ? NOT
				 : s.equals("-") ? MINUS
				 : null;
		}
	};
	
	protected UnaryOperator operator;
	protected Expression expression;
	
	private boolean isMinInt = false;
	public boolean isMinInt() { return this.isMinInt; }
	
	public UnaryExpression(ParseTree tree) {
		super(tree);
		assert(tree.numChildren() == 2);
		assert(tree.getSymbol().equals("UnaryExpression")
			|| tree.getSymbol().equals("UnaryExpressionNotPlusMinus"));

		this.operator = UnaryOperator.fromString(tree.getChildren()[0].getSymbol());
		this.expression = Expression.extractExpression(tree.getChildren()[1]);
		
		if (this.expression instanceof Literal
				&& ((Literal)this.expression).value.equals("2147483648")
				&& this.operator == UnaryOperator.MINUS) {
			this.isMinInt = true;
		}
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException, ImportException {
		this.environment = parentEnvironment;
		this.expression.buildEnvironment(this.environment);
	}

	@Override
	public void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException {
		this.expression.linkTypes(types);
	}

	@Override
	public void linkNames(TypeDecl curType, boolean staticCtx, EnvironmentDecl curDecl, Local curLocal, boolean lValue) throws NameLinkingException {
		this.expression.linkNames(curType, staticCtx, curDecl, curLocal, false);
	}

	@Override public void checkTypes() throws TypeCheckingException {
		this.expression.checkTypes();
		this.expression.assertNonVoid();
		Type eType = this.expression.getType();
		if (this.operator == UnaryOperator.NOT
				&& !eType.equals(PrimitiveType.BOOLEAN)) {
			throw new TypeCheckingException.TypeMismatch(this.expression, "boolean");
		}
		if (this.operator == UnaryOperator.MINUS
				&& !(eType instanceof PrimitiveType
						&& ((PrimitiveType)eType).isIntegral())) {
			throw new TypeCheckingException.TypeMismatch(this.expression, "an integral type");
		}
		this.exprType = eType;
	}
	
	@Override public Object asConstExpr() {
		if (this.isMinInt()) {
			return Integer.MIN_VALUE;
		}
		
		Object subExpr = this.expression.asConstExpr();
		if (subExpr == null)
			return null;
		if (subExpr instanceof Boolean)
			return !((Boolean)subExpr);
		if (subExpr instanceof Byte)
			return -((Byte)subExpr);
		if (subExpr instanceof Short)
			return -((Short)subExpr);
		assert (subExpr instanceof Integer);
		return -((Integer)subExpr);
	}
	
	// ---------- Code generation ----------
	
	@Override public void generateCode(AsmWriter writer, Frame frame) throws CodeGenerationException {
		
		if (this.isMinInt()) {
			writer.instr("mov", "eax", Integer.MIN_VALUE);
			return;
		}
		
		switch (this.operator) {
		  case NOT:
			this.expression.generateCode(writer, frame);
			writer.instr("xor", "eax", 1);
			break;
		  case MINUS:
			this.expression.generateCode(writer, frame);
			// Sign-extend by the correct amount, if less than 32 bits.
			switch (((PrimitiveType)this.expression.getType()).width()) {
			  case 1:
				writer.line("movsx eax, al  ; Sign extend 1 byte to 4 bytes");
				break;
			  case 2:
				writer.line("movsx eax, ax  ; Sign extend 2 bytes to 4 bytes");
				break;
			  default: // 4 bytes; do nothing
				break;
			}
			writer.instr("imul", "eax", -1);
			break;
		}
	}
}
