package Exceptions;

import java.util.List;

import AbstractSyntax.BinaryExpression;
import AbstractSyntax.ClassInstanceCreationExpression;
import AbstractSyntax.Constructor;
import AbstractSyntax.Expression;
import AbstractSyntax.FieldAccessExpression;
import AbstractSyntax.Identifier;
import AbstractSyntax.Method;
import AbstractSyntax.MethodInvocationExpression;
import AbstractSyntax.ReturnStatement;
import AbstractSyntax.TypeDecl;
import Types.Type;

public abstract class TypeCheckingException extends Exception {

	private static final long serialVersionUID = -3386514160728656658L;
	
	public TypeCheckingException(String msg) {
		super(msg);
	}

	public static class BadCtorName extends TypeCheckingException {

		private static final long serialVersionUID = -8156454042124170403L;
		
		public BadCtorName(Constructor ctor, TypeDecl clazz) {
			super(String.format("The constructor %s must share the name of its enclosing class %s.\n"
					+ " at %s\n",
					ctor.getName().toString(),
					clazz.getName().toString(),
					ctor.getPositionalString()));
		}
	}
	
	public static class MissingDefaultCtor extends TypeCheckingException {

		private static final long serialVersionUID = -8156454042124170404L;
		
		public MissingDefaultCtor(TypeDecl clazz) {
			super(String.format("The class %s has at least one constructor, and inherits from one with no default constructor.\n"
					+ " at %s\n",
					clazz.getName().toString(),
					clazz.getPositionalString()));
		}
	}
	
	public static class AbstractInstantiation extends TypeCheckingException {

		private static final long serialVersionUID = -8156454042124170405L;
		
		public AbstractInstantiation(ClassInstanceCreationExpression expr, TypeDecl clazz) {
			super(String.format("The abstract class %s cannot be instantiated.\n"
					+ " at %s\n",
					clazz.getName().toString(),
					expr.getPositionalString()));
		}
	}
	
	public static class BitwiseOperator extends TypeCheckingException {

		private static final long serialVersionUID = -8156454042124170406L;
		
		public BitwiseOperator(BinaryExpression expr) {
			super(String.format("Bitwise operations are not allowed.\n"
					+ " at %s\n",
					expr.getPositionalString()));
		}
	}
	
	public static class TypeMismatch extends TypeCheckingException {

		private static final long serialVersionUID = -8156454042124170407L;
		
		public TypeMismatch(Expression expr, String expected) {
			super(String.format("Expression has type %s; expected %s.\n"
					+ " at %s\n",
					expr.getType() == null ? "void" : expr.getType().getCanonicalName(),
					expected,
					expr.getPositionalString()));
		}
	}
	
	public static class ReturnTypeMismatch extends TypeCheckingException {

		private static final long serialVersionUID = -8156454042125170407L;
		
		public ReturnTypeMismatch(ReturnStatement stmnt, String expected) {
			super(String.format("Return expression should have type %s.\n"
					+ " at %s\n",
					expected,
					stmnt.getPositionalString()));
		}
	}
	
	public static class IllegalCast extends TypeCheckingException {

		private static final long serialVersionUID = -8156454042124170408L;
		
		public IllegalCast(Type sourceType, Identifier targetType) {
			super(String.format("Cannot cast type %s to %s.\n"
					+ " at %s\n",
					sourceType.getCanonicalName(),
					targetType.toString(),
					targetType.getPositionalString()));
		}
	}
	
	public static class IllegalFieldAccess extends TypeCheckingException {

		private static final long serialVersionUID = -8156454042124170409L;
		
		public IllegalFieldAccess(FieldAccessExpression expr) {
			super(String.format("Illegal field access.\n"
					+ " at %s\n",
					expr.getPositionalString()));
		}
	}
	
	public static class NoMethod extends TypeCheckingException {

		private static final long serialVersionUID = -8156454042124170410L;
		
		public NoMethod(MethodInvocationExpression expr) {
			super(String.format("No such method %s.\n"
					+ " at %s\n",
					expr,
					expr.getPositionalString()));
		}
	}
	
	public static class AmbiguousMethodInvocation extends TypeCheckingException {

		private static final long serialVersionUID = -8156454042124170411L;
		
		public AmbiguousMethodInvocation(MethodInvocationExpression expr, List<Method> matches) {
			super(formatString(expr, matches));
		}
		
		private static String formatString(MethodInvocationExpression expr, List<Method> matches) {
			String msg = String.format("Method %s is ambiguous in this context.\n"
					+ " at %s.\n Did you mean...", expr, expr.getPositionalString());
			for (Method method : matches) {
				msg += String.format("\n -> %s (declared in %s)", 
						method, 
						method.getPositionalString());
			}
			return msg;
		}
	}
	
	public static class NoConstructor extends TypeCheckingException {

		private static final long serialVersionUID = -8156454042124170412L;
		
		public NoConstructor(ClassInstanceCreationExpression expr) {
			super(String.format("No such constructor %s.\n"
					+ " at %s\n",
					expr,
					expr.getPositionalString()));
		}
	}
	
	public static class AmbiguousConstructorInvocation extends TypeCheckingException {

		private static final long serialVersionUID = -8156454042124170413L;
		
		public AmbiguousConstructorInvocation(ClassInstanceCreationExpression expr, List<Constructor> matches) {
			super(formatString(expr, matches));
		}
		
		private static String formatString(ClassInstanceCreationExpression expr, List<Constructor> matches) {
			String msg = String.format("Constructor %s is ambiguous in this context.\n"
					+ " at %s.\n Did you mean...", expr, expr.getPositionalString());
			for (Constructor ctor : matches) {
				msg += String.format("\n -> %s (declared in %s)", 
						ctor,
						ctor.getPositionalString());
			}
			return msg;
		}
	}
	
	public static class FinalFieldAssignment extends TypeCheckingException {

		private static final long serialVersionUID = -8156434042124170413L;
		
		public FinalFieldAssignment(FieldAccessExpression expr) {
			super(String.format("Assignment to final field length.\n"
					+ " at %s\n",
					expr.getPositionalString()));
		}

	}
	
}
