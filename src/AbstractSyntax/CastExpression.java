package AbstractSyntax;

import Parser.ParseTree;
import Types.PrimitiveType;
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
	
	@Override public Object asConstExpr() {
		if (this.type instanceof PrimitiveType) {
			
			Object constRhs = this.expression.asConstExpr();
			if (constRhs == null)
				return null;
			
			Type sourceType = this.expression.getType();
			if (!(sourceType instanceof PrimitiveType))
				return null;
			
			switch ((PrimitiveType)sourceType) {
			case BOOLEAN:
				switch ((PrimitiveType)this.type) {
				case BOOLEAN:
					return (Boolean)constRhs;
				default:
					return null;
				}
			case BYTE:
				switch ((PrimitiveType)this.type) {
				case CHAR:
					if (constRhs instanceof Integer)
						return (char)(byte)(int)(Integer)constRhs;
					return     (char)(byte)     (Byte)constRhs;
				case BYTE:
					if (constRhs instanceof Integer)
						return (byte)(int)(Integer)constRhs;
					return     (byte)     (Byte)constRhs;
				case INT:
					if (constRhs instanceof Integer)
						return (int)(byte)(int)(Integer)constRhs;
					return     (int)(byte)     (Byte)constRhs;
				case SHORT:
					if (constRhs instanceof Integer)
						return (short)(byte)(int)(Integer)constRhs;
					return     (short)(byte)     (Byte)constRhs;
				default:
					return null;
				}
			case CHAR:
				switch ((PrimitiveType)this.type) {
				case BYTE:
					return (byte)(char)(Character)constRhs;
				case CHAR:
					return (char)(Character)constRhs;
				case SHORT:
					return (short)(char)(Character)constRhs;
				case INT:
					return (int)(Character)constRhs;
				default:
					return null;
				}
			case INT:
				switch ((PrimitiveType)this.type) {
				case BYTE:
					return (byte)(int)(Integer)constRhs;
				case CHAR:
					return (char)(int)(Integer)constRhs;
				case INT:
					return (int)(Integer)constRhs;
				case SHORT:
					return (short)(int)(Integer)constRhs;
				default:
					return null;
				}
			case SHORT:
				switch ((PrimitiveType)this.type) {
				case BYTE:
					if (constRhs instanceof Integer)
						return (byte)(short)(int)(Integer)constRhs;
					return     (byte)(short)     (Short)constRhs;
				case CHAR:
					if (constRhs instanceof Integer)
						return (char)(short)(int)(Integer)constRhs;
					return     (char)(short)      (Short)constRhs;
				case INT:
					if (constRhs instanceof Integer)
						return (int)(short)(int)(Integer)constRhs;
					return     (int)(short)     (Short)constRhs;
				case SHORT:
					if (constRhs instanceof Integer)
						return (short)(int)(Integer)constRhs;
					return     (short)(Short)constRhs;
				default:
					return null;
				}
			}
		} 
		
		if (this.type == Program.javaLangString) {
			Object constRhs = this.expression.asConstExpr();
			return constRhs != null && constRhs instanceof String ? constRhs : null;
		}
		
		return null;
	}
}