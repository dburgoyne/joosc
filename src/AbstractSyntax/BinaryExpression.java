package AbstractSyntax;

import AbstractSyntax.Expression.ExpressionValue;
import AbstractSyntax.UnaryExpression.UnaryOperator;
import Parser.ParseTree;
import Types.PrimitiveType;
import Types.Type;
import Utilities.Cons;

public class BinaryExpression extends Expression {
	
	enum BinaryOperator {
		PLUS,
		MINUS,
		STAR,
		SLASH,
		MOD,
		AND,
		LAND,
		OR,
		LOR,
		XOR,
		EQ,
		NE,
		GT,
		LT,
		GE,
		LE,
		ASSIGN;
		
		public static BinaryOperator fromString(String s) {
			return s.equals("+") ? PLUS
				 : s.equals("-") ? MINUS
				 : s.equals("*") ? STAR
				 : s.equals("/") ? SLASH
				 : s.equals("%") ? MOD
				 : s.equals("&") ? AND
				 : s.equals("&&") ? LAND
				 : s.equals("|") ? OR
				 : s.equals("||") ? LOR
				 : s.equals("^") ? XOR
				 : s.equals("==") ? EQ
				 : s.equals("!=") ? NE
				 : s.equals(">") ? GT
				 : s.equals("<") ? LT
				 : s.equals(">=") ? GE
				 : s.equals("<=") ? LE
				 : s.equals("AssignmentOperator") ? ASSIGN
				 : null;
		}
	}
	
	protected Expression left, right;
	protected BinaryOperator operator;
	
	public BinaryExpression(ParseTree tree) {
		super(tree);
		this.left = Expression.extractExpression(tree.getChildren()[0]);
		this.operator = BinaryOperator.fromString(tree.getChildren()[1].getSymbol());
		this.right = Expression.extractExpression(tree.getChildren()[2]);
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException, ImportException {
		this.environment = parentEnvironment;
		
		this.left.buildEnvironment(this.environment);
		this.right.buildEnvironment(this.environment);
	}

	@Override
	public void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException {
		this.left.linkTypes(types);
		this.right.linkTypes(types);
	}

	@Override
	public void linkNames(TypeDecl curType, boolean staticCtx, EnvironmentDecl curDecl, Local curLocal, boolean lValue) throws NameLinkingException {
		this.left.linkNames(curType, staticCtx, curDecl, curLocal, (this.operator == BinaryOperator.ASSIGN));
		this.right.linkNames(curType, staticCtx, curDecl, curLocal, false);
	}
	
	@Override public void checkTypes() throws TypeCheckingException {
		// No bitwise operations may occur. 
		switch (this.operator) {
		  case AND:
		  case OR:
		  case XOR:
			throw new TypeCheckingException.BitwiseOperator(this);
	      default:
	    	// Do nothing
		}

		this.left.checkTypes();
		this.left.assertNonVoid();
		this.right.checkTypes();
		this.right.assertNonVoid();
		
		Type leftType = this.left.getType(),
			 rightType = this.right.getType();

		// Check operand types
		switch (this.operator) {
		  case PLUS:
			if (leftType == Program.javaLangString
					|| rightType == Program.javaLangString) break;
			// Else, fall through...
		  case MINUS:
		  case STAR:
		  case SLASH:
		  case MOD:
		  case GT:
		  case LT:
		  case GE:
		  case LE:
		  	if (!(leftType instanceof PrimitiveType
					&& ((PrimitiveType)leftType).isIntegral()))
				throw new TypeCheckingException.TypeMismatch(this.left, "an integral type");
			if (!leftType.canBeCastAs(rightType))
				throw new TypeCheckingException.TypeMismatch(this.right, leftType.getCanonicalName());
			break;

		  case LAND:
		  case LOR:
			if (leftType != PrimitiveType.BOOLEAN)
				throw new TypeCheckingException.TypeMismatch(this.left, "boolean");
			if (rightType != PrimitiveType.BOOLEAN)
				throw new TypeCheckingException.TypeMismatch(this.right, "boolean");
			break;

		  case EQ:
		  case NE:
			if (leftType instanceof PrimitiveType) {
				if (leftType == PrimitiveType.BOOLEAN) {
					if (rightType != PrimitiveType.BOOLEAN)
						throw new TypeCheckingException.TypeMismatch(this.right, "boolean");
				} else {
					if (!(rightType instanceof PrimitiveType
							&& rightType != PrimitiveType.BOOLEAN))
						throw new TypeCheckingException.TypeMismatch(this.right, "an integral type");
				}
			} else {
				if (rightType instanceof PrimitiveType)
					throw new TypeCheckingException.TypeMismatch(this.right, "a reference type");
				if (!rightType.canBeCastAs(leftType))
					throw new TypeCheckingException.TypeMismatch(this.right,leftType.getCanonicalName());
			}
			break;

		  case ASSIGN:
			// Array.length is the only final field in Joos.
			FieldAccessExpression fae = null;
			if (left instanceof FieldAccessExpression) fae = (FieldAccessExpression)left;
			if (left instanceof Identifier
					&& ((Identifier)left).getInterpretation() instanceof FieldAccessExpression)
				fae = (FieldAccessExpression)((Identifier)left).getInterpretation();
			if (fae != null && fae.field == null) {
				throw new TypeCheckingException.FinalFieldAssignment(fae);
			}
			if (!rightType.canBeAssignedTo(leftType))
				throw new TypeCheckingException.TypeMismatch(this.right,leftType.getCanonicalName());
			break;

		  default: break;
		}
		
		// Calculate result type
		switch (this.operator) {
		  case PLUS:
			if (leftType == Program.javaLangString
					|| rightType == Program.javaLangString) {
				this.exprType = Program.javaLangString;
				break;
			}
			// Else, fall through...
		  case MINUS:
		  case STAR:
		  case SLASH:
		  case MOD:
			this.exprType = PrimitiveType.INT; // Integral types are promoted to int.
			break;
			
		  case ASSIGN:
			this.exprType = leftType;
			break;
			
		  default:
			this.exprType = PrimitiveType.BOOLEAN;
			break;
		}
	}
	
	@Override
	public boolean isAlwaysTrue() {
		ExpressionValue ev = this.tryFetchValue();
		return (ev!=null && ev.boolValue());
	}
	
	@Override
	public boolean isAlwaysFalse() {
		ExpressionValue ev = this.tryFetchValue();
		return (ev!=null && !ev.boolValue());
	}
	
	@Override
	public ExpressionValue tryFetchValue() {
		ExpressionValue lEValue = left.tryFetchValue();
		ExpressionValue rEValue = right.tryFetchValue();
		if(lEValue != null && rEValue != null) {
			return lEValue.binaryOperate(rEValue, operator);
		} else {
			return null;
		}
	}
	
	// ---------- For code generate ----------

	@Override
	protected void setCommentName() {
		this.commentName = "";
	}
		
	@Override
	protected void selfGenerate() {
		// Nothing
	}
		
	@Override
	protected void hierarchyGenerate() {
		left.codeGenerate();
		System.out.println("push eax");
		right.codeGenerate();
		System.out.println("pop ebx");
		switch (this.operator) {
		case PLUS:
			System.out.println("add eax, ebx");
			break;
		case MINUS:
			System.out.println("sub eax, ebx");
			break;
		case STAR:
			System.out.println("imul eax, ebx"); // fix later
			break;
		case SLASH:
			System.out.println("idiv eax, ebx"); // fix later
			System.out.println("mov eax, al");
			break;
		case MOD:
			System.out.println("idiv eax, ebx"); // fix later
			System.out.println("mov eax, ah");
			break;
		case ASSIGN:
			System.out.println("mov [ebx], [eax]");
			break;
		default:
		}
	}
	
	@Override
	protected void finishGenerate() {
		// Nothing
	}
}
