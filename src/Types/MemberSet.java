package Types;

import Utilities.Cons;
import AbstractSyntax.*;


public class MemberSet {
	
	protected Cons<Field> inheritedFields;
	protected Cons<Field> declaredFields;
	
	protected Cons<Constructor> declaredCtors;

	protected Cons<Method> inheritedConcreteMethods;
	protected Cons<Method> declaredConcreteMethods;
	
	protected Cons<Method> inheritedAbstractMethods;
	protected Cons<Method> declaredAbstractMethods;

	public void inheritInterface(MemberSet ms) {

		assert ms.inheritedFields == null;
		assert ms.declaredFields == null;
		assert ms.declaredCtors == null;
		assert ms.inheritedConcreteMethods == null;
		assert ms.declaredConcreteMethods == null;
		
		this.inheritedAbstractMethods = 
			Cons.union(ms.declaredAbstractMethods,
				       Cons.union(ms.inheritedAbstractMethods,
						          this.inheritedAbstractMethods,
						          new Method.SameSignaturePredicate()),
					   new Method.SameSignaturePredicate());
	}
	
	public void inheritClass(MemberSet ms) {
		this.inheritedFields = 
				Cons.union(ms.declaredFields,
					       Cons.union(ms.inheritedFields,
							          this.inheritedFields,
							          new Field.SameNamePredicate()),
						   new Field.SameNamePredicate());

		this.inheritedConcreteMethods = 
				Cons.union(ms.declaredConcreteMethods,
					       Cons.union(ms.inheritedConcreteMethods,
							          this.inheritedConcreteMethods,
							          new Method.SameSignaturePredicate()),
						   new Method.SameSignaturePredicate());
		
		this.inheritedAbstractMethods = 
				Cons.union(ms.declaredAbstractMethods,
					       Cons.union(ms.inheritedAbstractMethods,
							          this.inheritedAbstractMethods,
							          new Method.SameSignaturePredicate()),
						   new Method.SameSignaturePredicate());
	}
	
	public void declareField(Field f) {
		this.declaredFields = new Cons<Field>(f, this.declaredFields);
	}
	
	public void declareConstructor(Constructor ctor) throws Exception {
		if (Cons.contains(this.declaredCtors, ctor,
				new Constructor.SameSignaturePredicate())) {
			new Exception.ConstructorSignatureClash(ctor);
		}
	}
	
	public static abstract class Exception extends java.lang.Exception {

		private static final long serialVersionUID = -7282341370266275537L;

		public Exception() {
			super();
		}

		public Exception(String message, Throwable cause,
				boolean enableSuppression, boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}

		public Exception(String message, Throwable cause) {
			super(message, cause);
		}

		public Exception(String message) {
			super(message);
		}

		public Exception(Throwable cause) {
			super(cause);
		}
		
		public static class InvalidReplacement extends Exception {
			private static final long serialVersionUID = -3706871865093487975L;
			
		}

		public static class ConstructorSignatureClash extends Exception {
			private static final long serialVersionUID = 1842787530386990067L;

			public ConstructorSignatureClash(Constructor ctor) {
				super(String.format("Constructor with signature %s " +
									"declared multiple times.\n at %s",
									ctor, ctor.getPositionalString()));
			}
		}

		public static class MethodSignatureClash extends Exception {
			private static final long serialVersionUID = 5532698722460233633L;
			
		}
		
		public static class UnimplementedMethod extends Exception {
			private static final long serialVersionUID = -4927799588585145353L;
			
		}
	}
}
