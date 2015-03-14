package AbstractSyntax;

import Parser.ParseTree;
import Types.Type;
import Utilities.Cons;

public class CastExpression extends Expression {
	
	protected Identifier typeName;
	protected Type type;
	protected Expression expression;
	
	public CastExpression(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("CastExpression"));
		
		this.typeName = new Identifier(tree.getChildren()[1]);
		this.expression = new UnaryExpression(tree.getChildren()[3]);
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException, ImportException {
		this.environment = parentEnvironment;
		
		this.typeName.buildEnvironment(this.environment);
		this.expression.buildEnvironment(this.environment);
	}

	@Override
	public void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException {
		this.type = this.typeName.resolveType(types, this.environment);
		this.expression.linkTypes(types);
	}
	
	@Override
	public void linkNames(TypeDecl curType, boolean staticCtx, EnvironmentDecl curDecl, Local curLocal, boolean lValue) throws NameLinkingException {
		this.expression.linkNames(curType, staticCtx, curDecl, curLocal, false);
	}

	@Override
	public void checkTypes() throws TypeCheckingException {

		this.expression.checkTypes();
		this.expression.assertNonVoid();
		Type sourceType = this.expression.getType();
		
		if (!sourceType.canBeCastAs(this.type)) {
			throw new TypeCheckingException.IllegalCast(sourceType, this.typeName);
		}

		this.exprType = this.type;
	}
}