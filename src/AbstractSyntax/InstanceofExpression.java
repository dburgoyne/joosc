package AbstractSyntax;

import Parser.ParseTree;
import Utilities.Cons;

public class InstanceofExpression extends Expression {
	
	protected Expression left;
	protected Identifier right;
	protected Type type;
	
	public InstanceofExpression(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("RelationalExpression"));
		
		this.left = Expression.extractExpression(tree.getChildren()[0]);
		this.right = new Identifier(tree.getChildren()[2]);
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException, ImportException {
		this.environment = parentEnvironment;
		
		this.left.buildEnvironment(this.environment);
		this.right.buildEnvironment(this.environment);
	}

	@Override
	public void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException {
		this.type = this.right.resolveType(types, this.environment);
		if (this.type instanceof PrimitiveType) {
			throw new TypeLinkingException.InstanceofPrimitive(type, 
					this.right.getPositionalString());
		}
		this.left.linkTypes(types);
	}
}
