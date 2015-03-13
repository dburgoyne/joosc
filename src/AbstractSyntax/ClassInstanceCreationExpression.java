package AbstractSyntax;

import java.util.List;
import java.util.ArrayList;

import Parser.ParseTree;
import Types.Type;
import Utilities.Cons;

public class ClassInstanceCreationExpression extends Expression {

	protected Identifier typeName;
	protected TypeDecl type;
	
	protected List<Expression> arguments;
	
	public ClassInstanceCreationExpression(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("ClassInstanceCreationExpression"));
		
		this.arguments = new ArrayList<Expression>();
		this.typeName = new Identifier(tree.getChildren()[1]);
		if (tree.numChildren() == 4) {
			// Do nothing.
		} else if (tree.numChildren() == 5) {
			extractArgumentList(tree.getChildren()[3]);
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
		
		this.typeName.buildEnvironment(this.environment);
		for (Expression argument : this.arguments) {
			argument.buildEnvironment(this.environment);
		}
	}

	@Override public void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException {
		Type type = this.typeName.resolveType(types, this.environment);
		if (!(type instanceof TypeDecl)) {
			throw new TypeLinkingException.NotRefType(type,
					this.typeName.getPositionalString());
		}
		this.type = (TypeDecl)type;
		for (Expression arg : this.arguments) {
			arg.linkTypes(types);
		}
	}

	@Override public void linkNames(TypeDecl curType, boolean staticCtx) throws NameLinkingException {
		for (Expression arg : this.arguments) {
			arg.linkNames(curType, staticCtx);
		}
	}
	
	@Override public void checkTypes() throws TypeCheckingException {
		// No objects of abstract classes may be created.
		if (this.type.isAbstract()) {
			throw new TypeCheckingException.AbstractInstantiation(this, this.type);
		}
		
		// Check that all accesses of protected fields, methods and constructors are in a
		// subtype of the type declaring the entity being accessed, or in the same package
		// as that type.
		// TODO
		
		for (Expression expr : this.arguments) {
			expr.checkTypes();
		}
	}
	
}
