package AbstractSyntax;

import AbstractSyntax.Identifier.Interpretation;
import Parser.ParseTree;
import Types.ArrayType;
import Types.NullType;
import Types.PrimitiveType;
import Utilities.Cons;
import Utilities.Predicate;

public class FieldAccessExpression extends Expression implements Interpretation {

	protected Expression primary;
	protected String fieldName;
	protected TypeDecl containingType;
	
	protected Field field; // Resolved in type check. null iff accessing <array>.length

	public FieldAccessExpression(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("FieldAccess"));
		
		this.primary = Expression.extractPrimary(tree.getChildren()[0]);
		this.fieldName = new Identifier(tree.getChildren()[2]).getSingleComponent();
	}
	
	// Construct from an Identifier interpreted as a non-static field access.
	public FieldAccessExpression(Identifier id, Expression expr, String fieldName) {
		super(id.parseTree);
		
		this.primary = expr;
		this.fieldName = fieldName;
		this.environment = id.environment;
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException, ImportException {
		this.environment = parentEnvironment;
		this.primary.buildEnvironment(this.environment);
	}

	@Override public void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException {
		this.primary.linkTypes(types);
	}

	@Override public void linkNames(TypeDecl curType, boolean staticCtx) throws NameLinkingException {
		this.containingType = curType;
		this.primary.linkNames(curType, staticCtx);
	}

	@Override
	public void checkTypes() throws TypeCheckingException {
		this.primary.checkTypes();
		this.primary.assertNonVoid();

		if (this.primary.getType() instanceof PrimitiveType
				|| this.primary.getType() instanceof NullType) {
			throw new TypeCheckingException.IllegalFieldAccess(this);
		}
		
		if (this.primary.getType() instanceof ArrayType) {
			if (this.fieldName.equals("length")) {
				this.exprType = PrimitiveType.INT;
			} else {
				throw new TypeCheckingException.IllegalFieldAccess(this);
			}
		}

		if (this.primary.getType() instanceof TypeDecl) {
			// Get all non-static fields matching this.fieldName.
			TypeDecl primaryType = (TypeDecl)this.primary.getType();
			Cons<Field> matches = Cons.filter(primaryType.memberSet.getFields(), new Predicate<Field>() {
				public boolean test(Field f) {
					return (f.name.toString().equals(FieldAccessExpression.this.fieldName)
						 && !f.modifiers.contains(Modifier.STATIC));
				}
			});
			
			// If !(we share the same package as, or are a subtype of, the class containing the field), then filter
			// out protected fields.
			if (!(primaryType.getPackageName().equals(this.containingType.getPackageName())
					|| this.containingType.isSubtypeOf(primaryType))) {
				matches = Cons.filter(matches, new Predicate<Field>() {
					public boolean test(Field f) {
						return !f.modifiers.contains(Modifier.PROTECTED);
					}
				});
			}
			
			// There must be exactly one match.
			if (matches == null || matches.tail != null) {
				throw new TypeCheckingException.IllegalFieldAccess(this);
			}
			
			this.field = matches.head;
			this.exprType = this.field.type;
		}
	}
}
