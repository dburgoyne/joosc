package Types;

import java.util.List;

import Types.MemberSet.Exception.MethodSignatureClash;
import Utilities.Cons;
import Utilities.Predicate;
import AbstractSyntax.*;


public class MemberSet {
	
	protected TypeDecl type;
	protected Cons<TypeDecl> supertypes;
	
	protected Cons<Field> inheritedFields;
	protected Cons<Field> declaredFields;
	
	protected Cons<Constructor> declaredCtors;

	protected Cons<Method> inheritedConcreteMethods;
	protected Cons<Method> declaredConcreteMethods;
	
	protected Cons<Method> inheritedAbstractMethods;
	protected Cons<Method> declaredAbstractMethods;

	public Cons<Field> getInheritedFields() { return inheritedFields; }
	public Cons<Field> getDeclaredFields() { return declaredFields; }
	public Cons<Field> getFields() { 
		return Cons.union(inheritedFields, declaredFields,
						  new Decl.SameNamePredicate());
	}
	public Cons<Method> getInstanceMethods() { 
		return Cons.filter(this.getMethods(), new Predicate<Method>() {
			public boolean test(Method m) {
				return !m.isStatic();
			}
		});
	}
	public Cons<Method> getMethods() { 
		return Cons.union(inheritedAbstractMethods, 
			   Cons.union(declaredAbstractMethods,
			   Cons.union(inheritedConcreteMethods,
					      declaredConcreteMethods,
				  new Method.SameSignaturePredicate()),
				  new Method.SameSignaturePredicate()),
				  new Method.SameSignaturePredicate());
	}
	
	public List<TypeDecl> getStrictSupertypes() {
		return Cons.toList(this.supertypes);
	}
	
	
	public MemberSet(TypeDecl type) {
		this.type = type;
	}

	public void inheritInterface(MemberSet ms) throws Exception {

		assert ms.inheritedFields == null;
		assert ms.declaredFields == null;
		assert ms.declaredCtors == null;
		assert ms.declaredConcreteMethods == null;
		
		this.inheritedAbstractMethods = 
			Cons.union(ms.declaredAbstractMethods,
				       Cons.union(ms.inheritedAbstractMethods,
						          this.inheritedAbstractMethods,
						          new Method.SameSignatureSameReturnTypeSameVisibilityPredicate()),
					   new Method.SameSignatureSameReturnTypeSameVisibilityPredicate());
		
		Cons<Method> toCheck = Cons.union(this.inheritedAbstractMethods,
										  this.inheritedConcreteMethods);
		while (toCheck != null) {
			Method method = toCheck.head;
			toCheck = toCheck.tail;
			// A class or interface must not contain (declare or inherit) two methods with the same signature but different return types.
			if (Cons.contains(toCheck, method,
					new Method.SameSignatureDifferentReturnTypePredicate())) {
				throw new Exception.MethodSignatureClash(method);
			}
			// A (concrete) protected method must not replace a public method.
			if (Cons.contains(toCheck, method,
					new Method.SameSignatureNarrowerVisibilityThanDifferentAbstractnessPredicate())) {
				throw new Exception.InvalidReplacement(method);
			}
		}
		
		this.supertypes = Cons.union(new Cons<TypeDecl>(ms.type, null), this.supertypes);
		
	}
	
	public void inheritClass(MemberSet ms) throws MethodSignatureClash {
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
		
		this.supertypes = Cons.union(new Cons<TypeDecl>(ms.type, ms.supertypes), this.supertypes);
	}
	
	public void declareField(Field f) {
		this.declaredFields = new Cons<Field>(f, this.declaredFields);
	}
	
	public void declareConstructor(Constructor ctor) throws Exception {
		if (Cons.contains(this.declaredCtors, ctor,
				new Constructor.SameSignaturePredicate())) {
			throw new Exception.ConstructorSignatureClash(ctor);
		}
		
		this.declaredCtors = new Cons<Constructor>(ctor, this.declaredCtors);
	}
	
	public void declareMethod(Method method) throws Exception {
		Cons<Method> allInheritedMethods = Cons.union(this.inheritedAbstractMethods, this.inheritedConcreteMethods);
		Cons<Method> allDeclaredMethods = Cons.union(this.declaredAbstractMethods, this.declaredConcreteMethods);
		Cons<Method> allMethods = Cons.union(allInheritedMethods, allDeclaredMethods);
				
		if (Cons.contains(allDeclaredMethods, method,
					new Method.SameSignaturePredicate())) {
			throw new Exception.MethodSignatureClash(method);
		}
		
		if (Cons.contains(allMethods, method,
				new Method.SameSignatureDifferentReturnTypePredicate())) {
			throw new Exception.InvalidReplacement(method);
		}
		
		// Cannot replace a non-static method with a static one.
		if (method.isStatic()) {
			if (Cons.contains(allInheritedMethods, method,
					new Method.SameSignatureNonStaticPredicate())) {
				throw new Exception.InvalidReplacement(method);
			}
		} else {
			// Cannot replace a static method with a non-static one.
			if (Cons.contains(allInheritedMethods, method,
					new Method.SameSignatureStaticPredicate())) {
				throw new Exception.InvalidReplacement(method);
			}
		}
		
		
		
		// Cannot replace a final method.
		if (Cons.contains(allInheritedMethods, method,
				new Method.SameSignatureFinalPredicate())) {
			throw new Exception.InvalidReplacement(method);
		}
		
		// A protected method must not replace a public method.
		if (method.isProtected()) {
			if (Cons.contains(allInheritedMethods, method,
					new Method.SameSignaturePublicPredicate())) {
				throw new Exception.InvalidReplacement(method);
			}
		}
		
		// Actually remove hidden methods from the lists.
		final Method methodCopy = method;
		this.inheritedAbstractMethods= Cons.filter(this.inheritedAbstractMethods, new Predicate<Method>() {
			public boolean test(Method otherMethod) {
				return !(new Method.SameSignaturePredicate().test(methodCopy, otherMethod));
			}});
		this.inheritedConcreteMethods= Cons.filter(this.inheritedConcreteMethods, new Predicate<Method>() {
			public boolean test(Method otherMethod) {
				return !(new Method.SameSignaturePredicate().test(methodCopy, otherMethod));
			}});
		
		if (method.isAbstract()) {
			this.declaredAbstractMethods = new Cons<Method>(method, this.declaredAbstractMethods);
		} else {
			this.declaredConcreteMethods = new Cons<Method>(method, this.declaredConcreteMethods);
		}
		
	}
	
	public void validate() throws Exception {
		if (this.type.isClass() && !this.type.isAbstract()) {
			// Cannot declare or inherit any abstract methods
			Cons<Method> allConcreteMethods = Cons.union(this.inheritedConcreteMethods, this.declaredConcreteMethods);
			
			if (this.declaredAbstractMethods != null) {
				throw new Exception.UnimplementedMethod(this.type, this.declaredAbstractMethods.head);
			}
			
			Cons<Method> toCheck = this.inheritedAbstractMethods;
			while (toCheck != null) {
				Method inherited = toCheck.head;
				toCheck = toCheck.tail;
				if (!Cons.contains(allConcreteMethods, inherited,
						new Method.SameSignatureSameReturnTypePredicate())) {
					throw new Exception.UnimplementedMethod(this.type, inherited);
				}
			}
		}
		
		Cons<Method> toCheck = Cons.union(this.inheritedAbstractMethods,
				               Cons.union(this.inheritedConcreteMethods,
						       Cons.union(this.declaredAbstractMethods,
								          this.declaredConcreteMethods)));
		// Remember the original list for illegal replacement checking.
		Cons<Method> allToCheck = toCheck;

		while (toCheck != null) {
			final Method method = toCheck.head;
			toCheck = toCheck.tail;
			// A class or interface must not contain (declare or inherit) two methods with the same signature but different return types.
			if (Cons.contains(toCheck, method,
					new Method.SameSignatureDifferentReturnTypePredicate())) {
				throw new Exception.MethodSignatureClash(method);
			}
			// A protected method must not replace a public method.
			if (Cons.contains(allToCheck, method,
					new Method.SameSignatureNarrowerVisibilityThanDifferentAbstractnessPredicate())) {
				throw new Exception.InvalidReplacement(method);
			}
			
			// Remove hidden methods
			Predicate<Method> hiddenByMethod = new Predicate<Method>() {
				public boolean test(Method otherMethod) {
					return method == otherMethod
							|| !new Method.SameSignaturePredicate().test(method, otherMethod)
							|| new Method.SameSignatureNarrowerVisibilityThanDifferentAbstractnessPredicate().test(otherMethod, method);
				}};
			this.inheritedAbstractMethods = Cons.filter(this.inheritedAbstractMethods, hiddenByMethod);
			this.inheritedConcreteMethods = Cons.filter(this.inheritedConcreteMethods, hiddenByMethod);
			this.declaredAbstractMethods = Cons.filter(this.declaredAbstractMethods, hiddenByMethod);
			this.declaredConcreteMethods = Cons.filter(this.declaredConcreteMethods, hiddenByMethod);
			toCheck = Cons.filter(toCheck, hiddenByMethod);
		}
	}

	
	public static abstract class Exception extends java.lang.Exception {

		private static final long serialVersionUID = -7282341370266275537L;

		public Exception() {
			super();
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
			
			public InvalidReplacement(Method method) {
				super(String.format("Method with signature %s " +
						"illegally replaces another.\n at %s",
						method, method.getPositionalString()));
			}
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
			public MethodSignatureClash(Method method) {
				super(String.format("Method with signature %s " +
						"clashes with another.\n at %s",
						method, method.getPositionalString()));
			}

			private static final long serialVersionUID = 5532698722460233633L;
			
		}
		
		public static class UnimplementedMethod extends Exception {
			private static final long serialVersionUID = -4927799588585145353L;
			
			public UnimplementedMethod(TypeDecl type, Method method) {
				super(String.format("Non-abstract class %s does not implement %s.\n"
						+ "at %s",
						type, method, type.getPositionalString()));
			}
			
		}
	}
}
