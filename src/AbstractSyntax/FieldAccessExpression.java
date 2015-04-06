package AbstractSyntax;

import AbstractSyntax.Identifier.Interpretation;
import CodeGeneration.AsmWriter;
import CodeGeneration.Frame;
import Exceptions.CodeGenerationException;
import Exceptions.ImportException;
import Exceptions.NameConflictException;
import Exceptions.NameLinkingException;
import Exceptions.TypeCheckingException;
import Exceptions.TypeLinkingException;
import Parser.ParseTree;
import Types.ArrayType;
import Types.NullType;
import Types.PrimitiveType;
import Utilities.BiPredicate;
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

	@Override public void linkNames(TypeDecl curType, boolean staticCtx, EnvironmentDecl curDecl, Local curLocal, boolean lValue) throws NameLinkingException {
		this.containingType = curType;
		this.primary.linkNames(curType, staticCtx, curDecl, curLocal, false);
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

			// If x : X <: Y has protected field Y.f,
			// 	We may access x.f   iff   We <: Y and X <: We or pkg(We) == pkg(Y)
			final TypeDecl We = this.containingType;
			final Identifier pkgWe = We.getPackageName();
			final TypeDecl X = primaryType;
			matches = Cons.filter(matches, new Predicate<Field>() {
				public boolean test(Field f) {
					if (!f.modifiers.contains(Modifier.PROTECTED))
						return true;
					
					TypeDecl Y = f.declaringType;
					Identifier pkgY = Y.getPackageName();
					if (new BiPredicate.Equality<Identifier>().test(pkgWe, pkgY))
						return true;
					
					return We.isSubtypeOf(Y) && X.isSubtypeOf(We);
				}
			});
			
			// There must be exactly one match.
			if (matches == null || matches.tail != null) {
				throw new TypeCheckingException.IllegalFieldAccess(this);
			}
			
			this.field = matches.head;
			this.exprType = this.field.type;
		}
	}
	
	// ---------- Code generation ----------
	
	private void generateCommon(AsmWriter writer, Frame frame, String instr) throws CodeGenerationException {
		this.primary.generateCode(writer, frame); // eax <- the object
		
		int byteOffset;
		
		if (this.field == null) {
			byteOffset = 8; // array .length is in third dword
		} else {
			byteOffset = this.field.byteOffset;
		}
		
		assert byteOffset >= 4;
		
		writer.instr("cmp", "eax", 0); // fail if object is null
		writer.instr("je",    "__exception");
		writer.justUsedGlobal("__exception");
		
		writer.instr(instr, "eax", "[eax + " + byteOffset + "]"); // eax <- *?(eax + byteOffset)
	}
	
	@Override public void generateCode(AsmWriter writer, Frame frame) throws CodeGenerationException {
		this.generateCommon(writer, frame, "mov");
	}
	
	@Override public void generateLValueCode(AsmWriter writer, Frame frame) throws CodeGenerationException {
		this.generateCommon(writer, frame, "lea");
	}
}
