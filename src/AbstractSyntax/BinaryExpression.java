package AbstractSyntax;

import java.util.ArrayList;
import java.util.List;

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
		  case AND:
		  case OR:
		  case XOR:
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
	
	@Override public Object asConstExpr() {
		Object subLeft = this.left.asConstExpr();
		if (subLeft == null)
			return null;
		
		Object subRight = this.right.asConstExpr();
		if (subRight == null)
			return null;

		String strLeft = subLeft instanceof String ? (String)subLeft : null;
		String strRight = subRight instanceof String ? (String)subRight : null;
		Boolean boolLeft = subLeft instanceof Boolean ? (Boolean)subLeft : null;
		Boolean boolRight = subRight instanceof Boolean ? (Boolean)subRight : null;
		Integer intLeft = subLeft instanceof Integer ? (Integer)(int)(Integer)subLeft
						: subLeft instanceof Short ? (Integer)(int)(short)(Short)subLeft
						: subLeft instanceof Byte ? (Integer)(int)(byte)(Byte)subLeft
						: subLeft instanceof Character ? (Integer)(int)(char)(Character)subLeft
						: (Integer)null;
		Integer intRight = subRight instanceof Integer ? (Integer)(int)(Integer)subRight
						 : subRight instanceof Short ? (Integer)(int)(short)(Short)subRight
						 : subRight instanceof Byte ? (Integer)(int)(byte)(Byte)subRight
						 : subRight instanceof Character ? (Integer)(int)(char)(Character)subRight
						 : (Integer)null;
		
		switch (this.operator) {
		  case PLUS:
			if (subLeft instanceof String || subRight instanceof String)
				return subLeft.toString() + subRight;
			assert intLeft != null && intRight != null;
			return (int)intLeft + (int)intRight;
		  case MINUS:
			assert intLeft != null && intRight != null;
		    return (int)intLeft - (int)intRight;	
		  case STAR:
			assert intLeft != null && intRight != null;
			return (int)intLeft * (int)intRight;	
		  case SLASH:
			assert intLeft != null && intRight != null;
			return (int)intLeft / (int)intRight;	
		  case MOD:
			assert intLeft != null && intRight != null;
			return (int)intLeft % (int)intRight;	
		  case GT:
			assert intLeft != null && intRight != null;
			return (int)intLeft > (int)intRight;	
		  case LT:
			assert intLeft != null && intRight != null;
			return (int)intLeft < (int)intRight;	
		  case GE:
			assert intLeft != null && intRight != null;
			return (int)intLeft >= (int)intRight;	
		  case LE:
			assert intLeft != null && intRight != null;
			return (int)intLeft <= (int)intRight;	

		  case LAND:
		  case AND:
		    assert boolLeft != null && boolRight != null;
		    return (boolean)boolLeft && (boolean)boolRight;
		  case LOR:
		  case OR:
		    assert boolLeft != null && boolRight != null;
		    return (boolean)boolLeft || (boolean)boolRight;
		    
		  case EQ:
			if (strLeft != null)
				return strLeft.equals(strRight);
			if (intLeft != null)
				return intLeft.equals(intRight);
			if (boolLeft != null)
				return boolLeft.equals(boolRight);
			return null;
		  case NE:
			if (strLeft != null)
				return !strLeft.equals(strRight);
			if (intLeft != null)
				return !intLeft.equals(intRight);
			if (boolLeft != null)
				return !boolLeft.equals(boolRight);
			return null;
		  default:
			return null;
		}
	}
	
	// ---------- Code generation ----------
	
	@Override public void generateCode(AsmWriter writer, Frame frame) throws CodeGenerationException {
		
		String label;
		
		// String appends and assignments are the only special cases
		if (this.operator == BinaryOperator.PLUS &&
				(this.left.getType() == Program.javaLangString || this.right.getType() == Program.javaLangString)) {
			// Evaluate and call java.lang.String.valueOf on both arguments, to convert to non-null Strings.
			this.left.generateCode(writer, frame);
			writer.instr("push", "eax");
			List<String> params = new ArrayList<String>();
			if (this.left.getType() instanceof PrimitiveType || this.left.getType() == Program.javaLangString) {
				params.add(this.left.getType().getCanonicalName());
			} else {
				params.add(Program.javaLangObject.getCanonicalName());
			}
			String valueOfLabel = Utilities.Label.generateLabel("sm",
					Program.javaLangString.getCanonicalName(),
					"valueOf",
					params);
			writer.instr("call", valueOfLabel);
			writer.justUsedGlobal(valueOfLabel);
			writer.instr("push", "eax");
			
			this.right.generateCode(writer, frame);
			writer.instr("push", "eax");
			params.clear();
			if (this.right.getType() instanceof PrimitiveType || this.right.getType() == Program.javaLangString) {
				params.add(this.right.getType().getCanonicalName());
			} else {
				params.add(Program.javaLangObject.getCanonicalName());
			}
			valueOfLabel = Utilities.Label.generateLabel("sm",
					Program.javaLangString.getCanonicalName(),
					"valueOf",
					params);
			writer.instr("call", valueOfLabel);
			writer.justUsedGlobal(valueOfLabel);
			writer.instr("push", "eax");
			
			params.clear();
			params.add(Program.javaLangString.getCanonicalName());
			String concatLabel = Utilities.Label.generateLabel("im",
					Program.javaLangString.getCanonicalName(),
					"concat",
					params);
			writer.instr("call", concatLabel);
			writer.justUsedGlobal(concatLabel);

		} else if (this.operator == BinaryOperator.ASSIGN) {
			
			
			this.left.generateLValueCode(writer, frame);
			writer.instr("push", "eax");
			writer.instr("push", "ebx"); // may be a subtype table
			
			this.right.generateCode(writer, frame);

			// implicit widening conversion: 1->2|4, 2->4 bytes.
			if (this.left.getType() instanceof PrimitiveType) {
				assert this.right.getType() instanceof PrimitiveType;
				PrimitiveType tl = (PrimitiveType)this.left.getType();
				PrimitiveType tr = (PrimitiveType)this.right.getType();
				String mov = tl.isSigned() && tr.isSigned() ? "movsx" : "movzx";
				
				if        (tl.width() == 2 && tr.width() == 1) {
					writer.instr(mov, "ax", "al");
				} else if (tl.width() == 4 && tr.width() == 1) {
					writer.instr(mov, "eax", "al");
				} else if (tl.width() == 4 && tr.width() == 2) {
					writer.instr(mov, "eax", "ax");
				}
			}
			
			writer.instr("pop", "ebx");
			
			// Dynamically check type of new insertion into array.
			if (this.left instanceof ArrayAccessExpression
					&& this.left.getType() instanceof TypeDecl) {
				// ebx <- array's inner type's subtype table
				// Now need to check that [eax] is in ebx (lhs array's inner type's subtype table)
				
				writer.instr("mov", "ecx", "[eax]"); // ecx <- rhs's Type ID
				writer.instr("cmp", "[ecx*4 + ebx]", "dword 0"); // lookup ecx in ebx
				writer.instr("je", "__exception");
				writer.justUsedGlobal("__exception");
			}
			
			writer.instr("pop", "ebx"); // ebx <- lhs's lvalue
			writer.instr("mov", "[ebx]", "eax");
		} else if (this.operator == BinaryOperator.LAND
				|| this.operator == BinaryOperator.LOR) {
			// Short-circuit operators.
			
			String shortCircuitLbl = Utilities.Label.generateLabel("binary_short_");
			
			// false && rhs = false, true || rhs = true
			this.left.generateCode(writer, frame);
			writer.instr("cmp", "al", this.operator == BinaryOperator.LAND ? 0 : 1);
			writer.instr("je", shortCircuitLbl);
			
			// true && rhs = false || rhs = rhs:
			this.right.generateCode(writer, frame);
			writer.label(shortCircuitLbl);
			
		} else {
			// All other cases (with eager evaluation) start the same way.
			
			generateSignExtend(writer, frame, this.left);
			writer.instr("push", "eax");
			generateSignExtend(writer, frame, this.right);
			// Convenient to have the left expression in eax
			writer.instr("mov", "ebx", "eax"); // ebx <- rhs
			writer.instr("pop", "eax");        // eax <- lhs
			
			switch (this.operator) {
			  case PLUS:
				writer.instr("add", "eax", "ebx");
				break;
			  case MINUS:
				writer.instr("sub", "eax", "ebx");
				break;
			  case STAR:
				// Only the lower 32 bits of the product make it into eax
				writer.instr("imul", "ebx");
				break;
			  case SLASH:
				// Throw exception on division by zero.
				writer.instr("cmp", "ebx", 0);
				writer.instr("je", "__exception");
				writer.justUsedGlobal("__exception");
				
				writer.instr("cdq");
				writer.instr("idiv", "ebx");
				break;
			  case MOD:
				// Throw exception on division by zero.
				writer.instr("cmp", "ebx", 0);
				writer.instr("je", "__exception");
				writer.justUsedGlobal("__exception");
				  
				writer.instr("cdq");
				writer.instr("idiv", "ebx");
				writer.instr("mov", "eax", "edx");
				break;
			  case GT:
				label = Utilities.Label.generateLabel("binary_gt");
				writer.instr("cmp", "eax", "ebx");
				writer.instr("mov", "eax", "1");
				writer.instr("jg", label);
				writer.instr("mov", "eax", "0");
				writer.label(label);
				break;
			  case LT:
			    label = Utilities.Label.generateLabel("binary_lt");
				writer.instr("cmp", "eax", "ebx");
				writer.instr("mov", "eax", "1");
				writer.instr("jl", label);
				writer.instr("mov", "eax", "0");
				writer.label(label);
				break;
			  case GE:
			    label = Utilities.Label.generateLabel("binary_ge");
				writer.instr("cmp", "eax", "ebx");
				writer.instr("mov", "eax", "1");
				writer.instr("jge", label);
				writer.instr("mov", "eax", "0");
				writer.label(label);
				break;
			  case LE:
			    label = Utilities.Label.generateLabel("binary_le");
				writer.instr("cmp", "eax", "ebx");
				writer.instr("mov", "eax", "1");
				writer.instr("jle", label);
				writer.instr("mov", "eax", "0");
				writer.label(label);
				break;
			  case AND:
				writer.instr("and", "eax,ebx");
				break;
			  case OR:
				writer.instr("or", "eax,ebx");
				break;
			  case XOR:
				writer.instr("xor", "eax,ebx");
				break;
			  case EQ:
			    label = Utilities.Label.generateLabel("binary_eq");
				writer.instr("cmp", "eax", "ebx");
				writer.instr("mov", "eax", "1");
				writer.instr("je", label);
				writer.instr("mov", "eax", "0");
				writer.label(label);
				break;
			  case NE:
				label = Utilities.Label.generateLabel("binary_ne");
				writer.instr("cmp", "eax", "ebx");
				writer.instr("mov", "eax", "1");
				writer.instr("jne", label);
				writer.instr("mov", "eax", "0");
				writer.label(label);
				break;

			  default: break;
			}
		}
	}

	private void generateSignExtend(AsmWriter writer, Frame frame, Expression e)
			throws CodeGenerationException {
		e.generateCode(writer, frame);
		if (e.getType() instanceof PrimitiveType) {
			PrimitiveType pt = (PrimitiveType)e.getType();
			String mov = pt.isSigned() ? "movsx" : "movzx";
			// Sign-extend the LHS by the correct amount, if less than 32 bits.
			switch (pt.width()) {
			  case 1:
				writer.line("%s eax, al  ; Sign extend 1 byte to 4 bytes", mov);
				break;
			  case 2:
				writer.line("%s eax, ax  ; Sign extend 2 bytes to 4 bytes", mov);
				break;
			  default: // 4 bytes; do nothing
				break;
			}
		}
	}
}
