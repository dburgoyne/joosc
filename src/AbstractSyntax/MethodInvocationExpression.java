package AbstractSyntax;

import java.util.ArrayList;
import java.util.List;

import Parser.ParseTree;
import Types.Type;
import Utilities.Cons;

public class MethodInvocationExpression extends Expression {

	// Pre-name resolution:
	protected Expression primary;    // Can be null !!!
	protected Identifier methodName; // can be multi-part if primary is null.
	
	// Post-name resolution:
	protected Expression receivingExpr; // receiver if non-static method call
	protected TypeDecl   receivingType; // receiver if static method call
	protected String     message;       // the last component of methodName
	
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
		
		this.allTypes = types;
	}
	
	private Cons<TypeDecl> allTypes;
	@Override
	public void linkNames(TypeDecl curType, boolean staticCtx) throws NameLinkingException {
		
		this.message = methodName.getLastComponent();
		
		if (this.primary == null) { 
			if (!this.methodName.isSimple())  {
				// TODO can probably just use linkNames instead of resolveType...
				
				Identifier prefix = methodName.withoutLastComponent();
				try {
					// if this is a static call to a named type...
					Type t = prefix.resolveType(this.allTypes, this.environment);
					if (!(t instanceof TypeDecl))
						throw new NameLinkingException.NonexistentMethod(this.methodName);
					this.receivingType = (TypeDecl)t;
				} catch (TypeLinkingException e) {
					// if this is not a static call to a named type...
					this.receivingExpr = prefix;
					this.receivingExpr.linkNames(curType, staticCtx);
				}
			}
		} else {
			this.primary.linkNames(curType, staticCtx);
			this.receivingExpr = this.primary;
		}
		
		for (Expression arg : this.arguments) {
			arg.linkNames(curType, staticCtx);
		}
	}
}
