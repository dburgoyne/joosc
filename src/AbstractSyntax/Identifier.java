package AbstractSyntax;

import java.util.ArrayList;
import java.util.List;

import Parser.ParseTree;
import Types.ArrayType;
import Types.PrimitiveType;
import Types.Type;
import Utilities.Cons;
import static Utilities.PredUtils.*;
import Utilities.BiPredicate;
import Utilities.Predicate;
import Utilities.StringUtils;

public class Identifier extends Expression {

	protected List<String> components;
	
	// Equals and hashCode
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (!(obj instanceof Identifier)) return false;
		Identifier other = (Identifier)obj;
		return this.components.equals(other.components);
	}
	
	@Override
	public int hashCode() {
		return this.components.hashCode();
	}
	
	public String toString() {
		return StringUtils.join(components, ".");
	}
	
	// Convenience functions.
	protected boolean isArray() {
		return this.components.get(this.components.size() - 1).equals("[]");
	}
	
	protected boolean isStarImport() {
		return this.components.get(this.components.size() - 1).equals("*");
	}
	
	protected boolean isThis() {
		return this.isSimple() && this.getSingleComponent().equals("this");
	}
	
	protected List<String> getComponents() {
		return this.components;
	}
	
	protected boolean isSimple() {
		return this.components.size() == 1;
	}
	
	protected boolean isQualified() {
		return this.components.size() > 1;
	}
	
	protected String getSingleComponent() {
		assert(this.components.size() == 1);
		return this.components.get(0);
	}
	
	protected List<String> getPackageName() {
		List<String> packageName = new ArrayList<String>();
		for (int i = 0; i < this.components.size() - 1; i++) {
			packageName.add(this.components.get(i));
		}
		return packageName;
	}
	
	protected String getLastComponent() {
		assert this.components.size() >= 1;
		return this.components.get(this.components.size() - 1);
	}
	
	private ParseTree treeWithoutLastComponent = null;
	public Identifier withoutLastComponent() {
		if (treeWithoutLastComponent == null) {
			String msg = "Identifier.withoutLastComponent called on "
					   + "simple Identifier " + this;
			throw new IllegalStateException(msg);
		}
		
		assert !this.isSimple();
		Identifier id = new Identifier(this.treeWithoutLastComponent);
		id.environment = this.environment;
		id.emptyPackagePrefix = this.emptyPackagePrefix;
		return id;
	}
	
	// Flattens almost anything.
	public Identifier(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("AmbiguousName")
			|| tree.getSymbol().equals("PackageName")
			|| tree.getSymbol().equals("Type")
			|| tree.getSymbol().equals("ReferenceType")
			|| tree.getSymbol().equals("ArrayType")
			|| tree.getSymbol().equals("PrimitiveType")
			|| tree.getSymbol().equals("Expression")
			|| tree.isTerminal());
		this.components = new ArrayList<String>();
		
		// For Expressions, walk down the left spine until we hit the AmbiguousName.
		if (tree.getSymbol().equals("Expression")) {
			while (!tree.getSymbol().equals("AmbiguousName")) {
				tree = tree.getChildren()[0];
			}
		}
		
		// Type has a different grammatical structure than the others.
		if (tree.getSymbol().equals("Type")) {
			extractType(tree);
		} else if (tree.getSymbol().equals("ReferenceType")) {
			extractReferenceType(tree);
		} else if (tree.getSymbol().equals("ArrayType")) {
			extractArrayType(tree);
		} else if (tree.getSymbol().equals("PrimitiveType")) {
			extractPrimitiveType(tree);
		} else if (tree.getSymbol().equals("AmbiguousName") || tree.getSymbol().equals("PackageName")) {
			extractAmbiguousOrPackageName(tree);
		} else if (tree.isTerminal()) {
			this.components.add(0, tree.getToken().getLexeme());
		}
	}
	
	private void extractAmbiguousOrPackageName(ParseTree tree) {
		assert(tree.getSymbol().equals("AmbiguousName") || tree.getSymbol().equals("PackageName"));
		
		while (tree.numChildren() > 1) {
			this.components.add(0, tree.getChildren()[2].getToken().getLexeme());
			tree = tree.getChildren()[0];
			
			if (this.treeWithoutLastComponent == null)
				this.treeWithoutLastComponent = tree;
		}
		this.components.add(0, tree.getChildren()[0].getToken().getLexeme());
	}
	
	private void extractType(ParseTree tree) {
		assert(tree.getSymbol().equals("Type"));
		
		ParseTree firstChild = tree.getChildren()[0];
		if (firstChild.getSymbol().equals("PrimitiveType")) {
			extractPrimitiveType(firstChild);
		} else if (firstChild.getSymbol().equals("ReferenceType")) {
			extractReferenceType(firstChild);
		}
	}
	
	private void extractPrimitiveType(ParseTree tree) {
		assert(tree.getSymbol().equals("PrimitiveType"));
		
		components.add(0, tree.getToken().getLexeme());
	}
	
	private void extractReferenceType(ParseTree tree) {
		assert(tree.getSymbol().equals("ReferenceType"));
		
		ParseTree firstChild = tree.getChildren()[0];
		if (firstChild.getSymbol().equals("ReferenceTypeArray")) {
			extractArrayType(firstChild.getChildren()[0]);
		} else if (firstChild.getSymbol().equals("ReferenceTypeNonArray")) {
			extractAmbiguousOrPackageName(firstChild.getChildren()[0]);
		}
	}
	
	private void extractArrayType(ParseTree tree) {
		assert(tree.getSymbol().equals("ArrayType"));
		
		ParseTree firstChild = tree.getChildren()[0];
		if (firstChild.getSymbol().equals("PrimitiveType")) {
			extractPrimitiveType(firstChild);
			if (this.treeWithoutLastComponent == null)
				this.treeWithoutLastComponent = firstChild;
		} else if (firstChild.getSymbol().equals("ReferenceTypeNonArray")) {
			extractAmbiguousOrPackageName(firstChild.getChildren()[0]);
			if (this.treeWithoutLastComponent == null)
				this.treeWithoutLastComponent = firstChild.getChildren()[0];
		}
		components.add("[]");
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException {
		this.environment = parentEnvironment;
	}

	private Package emptyPackagePrefix; // corresponds to empty identifier
	@Override
	public void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException {
		this.emptyPackagePrefix = new Package(types);
	}
	
	// Type of possible interpretations of prefixes of expression Identifiers
	public static interface Interpretation { }
	public static class This implements Interpretation {
		public This(TypeDecl thisType) {
			this.type = thisType;
		}
		public TypeDecl type;
	}
	public static class Package implements Interpretation {
		public final Cons<TypeDecl> types;
		public final String prefix;
		public Package(Cons<TypeDecl> types, String prefix) {
			this.types = types;
			this.prefix = prefix;
		}
		public Package(Cons<TypeDecl> types) {
			this(types, "");
		}
		public Package withComponent(String comp) {
			final String prefix = this.prefix + comp + '.';
			Cons<TypeDecl> filtered = Cons.filter(types,
					new Predicate<TypeDecl>() {
				@Override public boolean test(TypeDecl t) {
					return t.getCanonicalName().startsWith(prefix);
				}
			});
			return new Package(filtered, prefix);
		}
	}
	
	private Interpretation interpretation;
	public Interpretation getInterpretation() {
		if (this.interpretation == null)
			throw new IllegalStateException("Identifier " + this + 
											" has not been interpreted.");
		return interpretation;
	}
	
	public void linkNames(TypeDecl curType, boolean staticCtx) 
			throws NameLinkingException {
		
		if (this.isSimple()) { // Base case, one component.
			
			// Is this 'this'?
			if (this.isThis()) {
				if (staticCtx)
					throw new NameLinkingException.BadStatic(this);
				this.interpretation = new This(curType);
				return;
			}
			
			// Is this a local var or param?
			Cons<EnvironmentDecl> matchingLocals = 
				Cons.filter(this.environment, 
							either(isa(Local.class))
							   .or(isa(Formal.class))
							 .and_(new Decl.HasNamePredicate(this)));
			if (matchingLocals != null) {
				this.interpretation = (Interpretation)matchingLocals.head;
				return;
			}
			
			// Is this a non-static field of the current class?
			Cons<Field> matchingFields = 
					Cons.filter(curType.memberSet.getFields(),
								new Decl.HasNamePredicate(this));
			if (matchingFields != null) {
				if (staticCtx)
					throw new NameLinkingException.BadStatic(this);
				if (matchingFields.head.modifiers.contains(Modifier.STATIC))
					throw new NameLinkingException.BadNonStatic(this);
				this.interpretation = matchingFields.head;
				return;
			}
			
			// Is this a type?
			try {
				Type t = this.resolveType(this.emptyPackagePrefix.types,
										  this.environment);
				if (!(t instanceof TypeDecl))
					throw new NameLinkingException.TypeAsExpr(this, t);
				
				this.interpretation = (TypeDecl)t;
				return;
			} catch (TypeLinkingException e) { /* Not a type */ }
			
			// Otherwise, this is (part of) a package name. 
			Package pkg = emptyPackagePrefix
							.withComponent(this.getSingleComponent());
			if (pkg.types == null) {
				// Does not name a package
				throw new NameLinkingException.NotFound(this);
			}
			this.interpretation = pkg;
			return;
			
		} else { // Inductive case, this has a prefix.
			
			final String last = this.getLastComponent();
			Identifier prefix = this.withoutLastComponent();
			prefix.linkNames(curType, staticCtx);
			Interpretation prefixI = prefix.getInterpretation();
			
			if (prefixI instanceof Package) {
				
				final Package pkg = (Package)prefixI;
				Cons<TypeDecl> typesNamedThis = Cons.filter(pkg.types,
						new Predicate<TypeDecl>() {
					@Override public boolean test(TypeDecl t) {
						return t.getCanonicalName()
								.equals(pkg.prefix + last);
					}
				});
				
				if (typesNamedThis == null) {
					// This does not name a type, so it may name a package.
					Package newPkg = pkg.withComponent(last);
					if (newPkg.types == null) {
						// Does not name a package
						throw new NameLinkingException.NotFound(this);
					}
					this.interpretation = newPkg;
					return;
					
				} else {
					// This names a type.
					this.interpretation = typesNamedThis.head;
					return;
				}
				
			} else if (prefixI instanceof TypeDecl) {
				final TypeDecl type = (TypeDecl)prefixI;
				
				// Is this a static field of the type?
				Cons<Field> matchingFields = 
						Cons.filter(type.memberSet.getFields(),
								new Predicate<Field>() {
							@Override public boolean test(Field t) {
								return t.getName().getSingleComponent()
										.equals(last)
									&& t.modifiers.contains(Modifier.STATIC);
							}
						});
				// Filter out protected fields if we can't see them.
				if (!(new BiPredicate.Equality<Identifier>().test(type.getPackageName(), curType.getPackageName())
						 || curType.isSubtypeOf(type))) {
					matchingFields = 
							Cons.filter(matchingFields,
									new Predicate<Field>() {
								@Override public boolean test(Field t) {
									return !t.modifiers.contains(Modifier.PROTECTED);
								}
							});
				}
				
				if (matchingFields == null) {
					throw new NameLinkingException.NotFound(this);
				}
				if (matchingFields.tail != null) {
					throw new NameLinkingException.AmbiguousName(this);
				}
				this.interpretation = matchingFields.head;
				return;
								
			} else if (prefixI instanceof Expression) {
				// Else, it's a non-static field lookup. Actual validity
				// checking is postponed to type checking stage. 
				
				Expression expr = (Expression)prefixI;
				FieldAccessExpression fae = new FieldAccessExpression(this, expr, last);
				fae.linkNames(curType, staticCtx);
				this.interpretation = fae;
				
				return;
				
			} else if (prefixI instanceof Formal
					|| prefixI instanceof Local
					|| prefixI instanceof Field) {
				// Same as above, but use the prefix identifier itself as
				// the target expression of the field access.

				FieldAccessExpression fae = new FieldAccessExpression(this, prefix, last);
				fae.linkNames(curType, staticCtx);
				this.interpretation = fae;
				return;
				
			} else if (prefixI instanceof This) {
				
				// Access a non-static field of the current class.
				// Don't have to check for protected - we are accessing this.something
				Cons<Field> matchingFields = 
						Cons.filter(curType.memberSet.getFields(),
								new Predicate<Field>() {
							@Override public boolean test(Field t) {
								return t.getName().getSingleComponent()
										.equals(last)
									&& !t.modifiers.contains(Modifier.STATIC);
							}
						});
				if (matchingFields == null) {
					throw new NameLinkingException.NotFound(this);
				}
				if (matchingFields.tail != null) {
					throw new NameLinkingException.AmbiguousName(this);
				}
				this.interpretation = matchingFields.head;
				return;
			}
			
			throw new AssertionError(prefixI + " is an unorthodox interpretation.");	
		}
	}
	
	public Type resolveType(Cons<TypeDecl> allTypes, Cons<EnvironmentDecl> localEnv) throws TypeLinkingException {
		
		// Special case, this denotes an array type:
		if (this.isArray()) {
			Identifier clone = new Identifier(this.parseTree);
			String removed = clone.components.remove(clone.components.size() - 1);
			assert(removed.equals("[]"));
			return new ArrayType(clone.resolveType(allTypes, localEnv));
		}
		
		// Special case, this denotes a primitive type.
		if (this.isSimple()) {
			PrimitiveType prim = PrimitiveType.fromString(this.getSingleComponent());
			if (prim != null) 
				return prim;
		}
		
		// General case, this should resolve to a TypeDecl:
		Cons<?> maybeTypeDecl = null;
		final List<String> packageName = this.getPackageName();
		final String typeName = this.getLastComponent();
		
		if (this.isQualified()) {
			// No prefix of our name may match a type.
			for (int i = 1; i < this.components.size(); i++) {
				final List<String> prefix = this.components.subList(0, i);
				Cons<?> typesMatchingPrefix =
					(i == 1)  // Is the prefix simple or qualified?
					? Cons.filter(this.environment,
						 new Predicate<EnvironmentDecl>() {
							 public boolean test(EnvironmentDecl decl) {
								 if (!(decl instanceof TypeDecl)) return false;
								 TypeDecl type = (TypeDecl)decl;
							 	 return type.getName().getComponents().equals(prefix);
							 }})
					: Cons.filter(allTypes,
						 new Predicate<TypeDecl>() {
							 public boolean test(TypeDecl type) {
								 return type.getName().getComponents().equals(prefix);
					 }});
				if (typesMatchingPrefix != null) {
					throw new TypeLinkingException.PrefixMatchesType(this, (TypeDecl)typesMatchingPrefix.head);
				}
			}
		}
		
		
		if (this.isQualified()) {
			maybeTypeDecl = Cons.filter(allTypes,
					new Predicate<TypeDecl>() {
						public boolean test(TypeDecl type) {
							Identifier typePackageName = type.getPackageName();
							return (typePackageName != null && typePackageName.getComponents().equals(packageName)
								 && type.getName().getLastComponent().equals(typeName));
						}
				});
		} else {
			maybeTypeDecl = Cons.filter(localEnv,
					new Predicate<EnvironmentDecl>() {
						public boolean test(EnvironmentDecl decl) {
							if (!(decl instanceof TypeDecl)) return false;
							TypeDecl type = (TypeDecl)decl;
							return type.getName().getLastComponent().equals(typeName);
						}
				});
		}
		
		if (maybeTypeDecl == null) {
			// Type linking failed.
			throw new TypeLinkingException.NoSuchType(this);
		}
		
		if (maybeTypeDecl.tail != null) {
			// Ambiguous type
			throw new TypeLinkingException.AmbiguousType(this, 
					Cons.toList(maybeTypeDecl).toArray(new TypeDecl[0]));
		}
		// Everything is OK
		assert(maybeTypeDecl.head instanceof TypeDecl);
		return (TypeDecl)maybeTypeDecl.head;
	}

	@Override
	public void checkTypes() throws TypeCheckingException {

		Interpretation interp = this.getInterpretation();
		
		if (interp instanceof This) {
			this.exprType = ((This)interp).type;
			return;
		}
		
		if (interp instanceof Local) {
			this.exprType = ((Local)interp).type;
			return;
		}
		
		if (interp instanceof Formal) {
			this.exprType = ((Formal)interp).type;
			return;
		}
		
		if (interp instanceof Field) {
			this.exprType = ((Field)interp).type;
			return;
		}
		
		if (interp instanceof FieldAccessExpression) {
			FieldAccessExpression expr = (FieldAccessExpression)interp;
			expr.checkTypes();
			expr.assertNonVoid();
			this.exprType = expr.getType();
			return;
		}
		
		if (interp instanceof TypeDecl) {
			// Can't use the identifier in an expression.
			return;
		}

		assert false;
	}

}
