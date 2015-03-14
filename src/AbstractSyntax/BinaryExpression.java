package AbstractSyntax;

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
	public void linkNames(TypeDecl curType, boolean staticCtx) throws NameLinkingException {
		this.left.linkNames(curType, staticCtx);
		this.right.linkNames(curType, staticCtx);
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
			if (!leftType.canCastTo(rightType))
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
			}
			break;

		  case ASSIGN:
			if (!rightType.canAssignTo(leftType))
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
}
