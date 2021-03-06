package AbstractSyntax;

import CodeGeneration.AsmWriter;
import CodeGeneration.Frame;
import Exceptions.CodeGenerationException;
import Exceptions.ImportException;
import Exceptions.NameConflictException;
import Exceptions.NameLinkingException;
import Exceptions.ReachabilityException;
import Exceptions.TypeCheckingException;
import Exceptions.TypeLinkingException;
import Parser.ParseTree;
import Types.Type;
import Utilities.Cons;

public class ReturnStatement extends Statement {
	protected Expression expression;  // Could be null!
	protected EnvironmentDecl containingDecl;
	
	public ReturnStatement(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("ReturnStatement"));
		
		if (tree.numChildren() == 2) {
			// Do nothing.
		} else if (tree.numChildren() == 3) {
			this.expression = Expression.extractExpression(tree.getChildren()[1]);
		}
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException, ImportException {
		this.environment = parentEnvironment;
		if (this.expression != null) {
			this.expression.buildEnvironment(this.environment);
		}
	}

	@Override
	public void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException {
		if (this.expression != null) {
			this.expression.linkTypes(types);
		}
	}
	
	@Override
	public void linkNames(TypeDecl curType, boolean staticCtx, EnvironmentDecl curDecl, Local curLocal, boolean lValue) throws NameLinkingException {
		this.containingDecl = curDecl;
		if (this.expression != null) {
			this.expression.linkNames(curType, staticCtx, curDecl, curLocal, false);
		}
	}

	@Override
	public void checkTypes() throws TypeCheckingException {
		Type eType = null;
		if (this.expression != null) {
			this.expression.checkTypes();
			this.expression.assertNonVoid();
			eType = this.expression.getType();
		}
		
		if (containingDecl instanceof Constructor) {
			if (eType != null) {
				throw new TypeCheckingException.ReturnTypeMismatch(this, "void");
			}
		} else {
			Method m = (Method)this.containingDecl;
			if ((eType == null) && (m.type != null)) {
				throw new TypeCheckingException.ReturnTypeMismatch(this, m.type.getCanonicalName());
			}
			if ((eType != null) && (m.type == null)) {
				throw new TypeCheckingException.ReturnTypeMismatch(this, "void");
			}
			if ((eType != null) && (m.type != null)) {
				// eType must be assignable to m.type
				if (!eType.canBeAssignedTo(m.type)) {
					throw new TypeCheckingException.ReturnTypeMismatch(this, m.type.getCanonicalName());
				}
			}
		}
	}
	
	@Override public void checkReachability(boolean canLeavePrevious) throws ReachabilityException {
		this.canEnter = canLeavePrevious;
		if (!this.canEnter) {
			throw new ReachabilityException.UnreachableStatement(this);
		}
		this.canLeave = false;
	}
	
	// ---------- Code generation ----------
	
	@Override public void generateCode(AsmWriter writer, Frame frame) throws CodeGenerationException {
		
		// Evaluate the return expression, if any.
		if (this.expression != null) {
			this.expression.generateCode(writer, frame);
		}
		
		// Leave all frames we're in, and return while popping the correct number of arguments from the stack.
		frame.ret(writer);
		
	}
}
