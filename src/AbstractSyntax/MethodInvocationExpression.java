package AbstractSyntax;

import java.util.ArrayList;
import java.util.List;

import Exceptions.ImportException;
import Exceptions.NameConflictException;
import Exceptions.NameLinkingException;
import Exceptions.TypeCheckingException;
import Exceptions.TypeLinkingException;
import Parser.ParseTree;
import Utilities.BiPredicate;
import Utilities.Cons;

public class MethodInvocationExpression extends Expression {

	// Pre-name resolution:
	protected Expression primary;    // Can be null !!!
	protected Identifier methodName; // can be multi-part if primary is null.
	
	// Post-name resolution:
	protected Expression receivingExpr; // receiver if non-static method call
	protected TypeDecl   receivingType; // receiver if static method call
	protected String     message;       // the last component of methodName
	protected TypeDecl   containingType;

	// Post-type checking:
	protected Method method;
	
	protected List<Expression> arguments;
	
	public MethodInvocationExpression(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("MethodInvocation"));
		
		this.arguments = new ArrayList<Expression>();
		if (tree.numChildren() == 3) {
			this.methodName = new Identifier(tree.getChildren()[0]);
		} else if (tree.numChildren() == 4) {
			this.methodName = new Identifier(tree.getChildren()[0]);
			extractArgumentList(tree.getChildren()[2]);
		} else if (tree.numChildren() == 5) {
			this.primary = Expression.extractExpression(tree.getChildren()[0]);
			this.methodName = new Identifier(tree.getChildren()[2]);
		} else if (tree.numChildren() == 6) {
			this.primary = Expression.extractExpression(tree.getChildren()[0]);
			this.methodName = new Identifier(tree.getChildren()[2]);
			extractArgumentList(tree.getChildren()[4]);
		}
	}
	
	private void extractArgumentList(ParseTree tree) {
		assert(tree.getSymbol().equals("ArgumentList"));
		
		while (tree.numChildren() == 3) {
			Expression expr = Expression.extractExpression(tree.getChildren()[2]);
			this.arguments.add(0, expr);
			tree = tree.getChildren()[0];
		}
		Expression expr = Expression.extractExpression(tree.getChildren()[0]);
		this.arguments.add(0, expr);
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException, ImportException {
		this.environment = parentEnvironment;
		if (this.primary != null) {
			this.primary.buildEnvironment(this.environment);
		}
		this.methodName.buildEnvironment(this.environment);
		for (Expression argument : this.arguments) {
			argument.buildEnvironment(this.environment);
		}
	}

	@Override
	public void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException {
		if (this.primary != null) {
			this.primary.linkTypes(types);
		}
		this.methodName.linkTypes(types);
		for (Expression arg : this.arguments) {
			arg.linkTypes(types);
		}
	}
	
	@Override
	public void linkNames(TypeDecl curType, boolean staticCtx, EnvironmentDecl curDecl, Local curLocal, boolean lValue) throws NameLinkingException {
		
		this.containingType = curType;
		this.message = methodName.getLastComponent();
		
		if (this.primary == null) { 
			if (this.methodName.isSimple()) {
				// The existence of this non-static method is checked at
				// type checking time.
				
				if (staticCtx) { // Check: static context => static usage.
					throw new NameLinkingException.BadNonStatic(this);
				}
			} else {
				
				Identifier prefix = methodName.withoutLastComponent();
				prefix.linkNames(curType, staticCtx, curDecl, curLocal, false);
				Identifier.Interpretation interp = prefix.getInterpretation();
				
				if (interp instanceof TypeDecl) {
					this.receivingType = (TypeDecl)interp;
					// The existence of this static method is checked at
					// type checking time, since overloading requires knowing
					// types of arguments. 
				} else if (interp instanceof Expression) {
					this.receivingExpr = (Expression)interp;
					this.receivingExpr.linkNames(curType, staticCtx, curDecl, curLocal, false);
					// The existence of this non-static method is checked at
					// type checking time.
				} else if (interp instanceof Local
						|| interp instanceof Formal
						|| interp instanceof Field
						|| interp instanceof Identifier.This) {
					this.receivingExpr = prefix;
					// The existence of this non-static method is checked at
					// type checking time.
					
					if (staticCtx && // Check: static context => static usage.
						(interp instanceof Identifier.This ||
						 interp instanceof Field
						 && !((Field)interp).modifiers.contains(Modifier.STATIC))) {
							throw new NameLinkingException.BadNonStatic(this);
					}
				} else {
					throw new NameLinkingException.NonexistentMethod(this.methodName);
				}
			}
		} else {
			this.primary.linkNames(curType, staticCtx, curDecl, curLocal, false);
			this.receivingExpr = this.primary;
		}
		
		for (Expression arg : this.arguments) {
			arg.linkNames(curType, staticCtx, curDecl, curLocal, false);
		}
	}

	@Override public void checkTypes() throws TypeCheckingException {
		if (this.receivingExpr != null) {
			this.receivingExpr.checkTypes();
			this.receivingExpr.assertNonVoid();
		}
		for (Expression arg : this.arguments) {
			arg.checkTypes();
			arg.assertNonVoid();
		}
		
		// What method is being called?
		List<Method> matches = new ArrayList<Method>();
		boolean staticCall = (this.receivingType != null);
		boolean implicitThis = (!staticCall && (this.receivingExpr == null));
		if (!staticCall && !implicitThis && !(this.receivingExpr.getType() instanceof TypeDecl)) {
			throw new TypeCheckingException.NoMethod(this);
		}
		TypeDecl owner = staticCall   ? this.receivingType
					   : implicitThis ? this.containingType
					   : (TypeDecl)this.receivingExpr.getType();

		loop: for (Method m : Cons.toList(owner.memberSet.getMethods())) {
			if (!m.name.getSingleComponent().equals(this.message)) {
				continue loop;
			}
			if (m.parameters.size() != this.arguments.size()) {
				continue loop;
			} else {
				for (int i = 0; i < m.parameters.size(); i++) {
					if (!m.parameters.get(i).type.equals(this.arguments.get(i).getType())) {
						continue loop;
					}
				}
				// All invocations of protected methods must be in a subtype of the type declaring the
				// method being accessed, or in the same package as that type.
				if (m.modifiers.contains(Modifier.PROTECTED)
						&& !(new BiPredicate.Equality<Identifier>()
								.test(owner.getPackageName(),
									  this.containingType.getPackageName())
							 || ((staticCall || owner.isSubtypeOf(this.containingType)) && this.containingType.isSubtypeOf(m.declaringType)))) {
					continue loop;
				}
				// Can't call static methods without qualifying them.
				if (staticCall != m.modifiers.contains(Modifier.STATIC)) {
					continue loop;
				}
			}
			matches.add(m);
		}
		
		// Make sure there is exactly one match.
		if (matches.size() == 0) {
			throw new TypeCheckingException.NoMethod(this);
		}
		if (matches.size() > 1) {
			throw new TypeCheckingException.AmbiguousMethodInvocation(this, matches);
		}
		this.method = matches.get(0);
		this.exprType = this.method.type;
	}
	
	public String toString() {
		return message;
	}
}
