package AbstractSyntax;

import java.util.List;
import java.util.ArrayList;

import CodeGeneration.AsmWriter;
import CodeGeneration.Frame;
import Exceptions.CodeGenerationException;
import Exceptions.ImportException;
import Exceptions.NameConflictException;
import Exceptions.NameLinkingException;
import Exceptions.TypeCheckingException;
import Exceptions.TypeLinkingException;
import Parser.ParseTree;
import Types.Type;
import Utilities.BiPredicate;
import Utilities.Cons;

public class ClassInstanceCreationExpression extends Expression {

	protected Identifier typeName;
	protected TypeDecl type;
	protected TypeDecl containingType;
	
	protected Constructor ctorCalled; // resolved during type checking.
	
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

	@Override public void linkNames(TypeDecl curType, boolean staticCtx, EnvironmentDecl curDecl, Local curLocal, boolean lValue) throws NameLinkingException {
		this.containingType = curType;
		for (Expression arg : this.arguments) {
			arg.linkNames(curType, staticCtx, curDecl, curLocal, false);
		}
	}
	
	@Override public void checkTypes() throws TypeCheckingException {
		// No objects of abstract classes may be created.
		if (this.type.isAbstract()) {
			throw new TypeCheckingException.AbstractInstantiation(this, this.type);
		}

		for (Expression expr : this.arguments) {
			expr.checkTypes();
			expr.assertNonVoid();
		}
		
		// What constructor is being called?
		List<Constructor> matches = new ArrayList<Constructor>();
		for (Constructor ctor : this.type.constructors) {
			boolean flag = true;
			if (ctor.parameters.size() != this.arguments.size()) {
				flag = false;
			} else {
				for (int i = 0; i < ctor.parameters.size(); i++) {
					if (!ctor.parameters.get(i).type.equals(this.arguments.get(i).getType())) {
						flag = false;
					}
				}
				// All accesses of protected constructors must be in the same package as that type.
				if (ctor.modifiers.contains(Modifier.PROTECTED)
						&& !new BiPredicate.Equality<Identifier>()
							.test(ctor.parent.getPackageName(),
								  this.containingType.getPackageName())) {
					flag = false;
				}
			}
			if (flag) {
				matches.add(ctor);
			}
		}
			
		// Make sure there is exactly one match.
		if (matches.size() == 0) {
			throw new TypeCheckingException.NoConstructor(this);
		}
		if (matches.size() > 1) {
			throw new TypeCheckingException.AmbiguousConstructorInvocation(this, matches);
		}
		this.ctorCalled = matches.get(0);
		
		this.exprType = this.type;
	}
	
	// ---------- Code generation ----------
	
	@Override public void generateCode(AsmWriter writer, Frame frame) throws CodeGenerationException {
		// Determine object size.
		writer.instr("mov", "eax", this.type.sizeOf());
		// Call __malloc
		writer.instr("call", "__malloc");
		writer.justUsedGlobal("__malloc");
		writer.instr("push", "eax");
		writer.instr("push", "eax");
		// Set the type ID in the first dword.
		writer.instr("mov", "[eax]", "dword " + this.type.getTypeID());
		// Zero the object
		for (int i = 4; i < this.type.sizeOf(); i += 4) {
			writer.instr("mov", "[eax + " + i + "]", "dword 0");
		}
		// Call the constructor.
		for (int i = 0; i < this.arguments.size(); i++) {
			this.arguments.get(i).generateCode(writer, frame);
			writer.instr("push", "eax");
		}
		writer.instr("call", this.ctorCalled.getLabel());
		writer.justUsedGlobal(this.ctorCalled.getLabel());
		// Pop the address of the new object.
		writer.instr("pop", "eax");
	}
}
