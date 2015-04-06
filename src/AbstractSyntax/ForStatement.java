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
import Types.PrimitiveType;
import Utilities.Cons;

public class ForStatement extends Statement {	
	
	protected BlockStatement initializer;
	protected Expression condition;
	protected Statement postExpression;
	protected Statement body;
	
	public ForStatement(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("ForStatement")
			|| tree.getSymbol().equals("ForStatementNoShortIf"));
		
		for (int index = 2; index < tree.numChildren() - 2; index++) {
			extractForLoopComponent(tree.getChildren()[index]);
		}
		this.body = Statement.extractStatement(tree.getChildren()[tree.numChildren()-1]);
	}
	
	private void extractForLoopComponent(ParseTree tree) {
		if (tree.getSymbol().equals("ForInit")) {
			this.initializer = BlockStatement.extractBlockStatement(tree);
		} else if (tree.getSymbol().equals("Expression")) {
			this.condition = Expression.extractExpression(tree);
		} else if (tree.getSymbol().equals("StatementExpression")) {
			this.postExpression = Expression.extractExpression(tree);
		} else if (tree.getSymbol().equals(";")) {
			// Do nothing.
		}
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException, ImportException {
		this.environment = parentEnvironment;
		
		if (this.initializer != null) {
			this.initializer.buildEnvironment(this.environment);
			EnvironmentDecl export = this.initializer.exportEnvironmentDecls();
			if (export != null) {
				this.environment = new Cons<EnvironmentDecl>(export, this.environment);
			}
		}
		if (this.condition != null) {
			this.condition.buildEnvironment(this.environment);
		}
		if (this.postExpression != null) {
			this.postExpression.buildEnvironment(this.environment);
		}
		assert(this.body != null);
		this.body.buildEnvironment(this.environment);
	}

	@Override public void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException {
		if (this.initializer != null) {
			this.initializer.linkTypes(types);
		}
		if (this.condition != null) {
			this.condition.linkTypes(types);
		}
		if (this.postExpression != null) {
			this.postExpression.linkTypes(types);
		}
		this.body.linkTypes(types);
	}

	@Override public void linkNames(TypeDecl curType, boolean staticCtx, EnvironmentDecl curDecl, Local curLocal, boolean lValue) throws NameLinkingException {
		if (this.initializer != null) {
			this.initializer.linkNames(curType, staticCtx, curDecl, curLocal, false);
		}
		if (this.condition != null) {
			this.condition.linkNames(curType, staticCtx, curDecl, curLocal, false);
		}
		if (this.postExpression != null) {
			this.postExpression.linkNames(curType, staticCtx, curDecl, curLocal, false);
		}
		this.body.linkNames(curType, staticCtx, curDecl, curLocal, false);
	}

	@Override public void checkTypes() throws TypeCheckingException {
		if (this.initializer != null) {
			this.initializer.checkTypes();
		}
		if (this.condition != null) {
			this.condition.checkTypes();
			this.condition.assertNonVoid();
			
			if (this.condition.getType() != PrimitiveType.BOOLEAN) {
				throw new TypeCheckingException.TypeMismatch(this.condition, "boolean");
			}
		}
		if (this.postExpression != null) {
			this.postExpression.checkTypes();
		}
		this.body.checkTypes();
	}
	
	@Override public void checkReachability(boolean canLeavePrevious) throws ReachabilityException {
		this.canEnter = canLeavePrevious;
		if (!this.canEnter) {
			throw new ReachabilityException.UnreachableStatement(this);
		}
		
		if (this.initializer != null) {
			this.initializer.checkReachability(true);
		}
		if (this.condition != null) {
			this.condition.checkReachability(true);
		}
		if (this.postExpression != null) {
			this.postExpression.checkReachability(true);
		}
		// Body is unreachable if the condition is present and always false.
		this.body.checkReachability(!(this.condition != null && this.condition.isAlwaysFalse()));
		
		// Can leave the loop only if we can leave the body and the condition is not always true.
		this.canLeave = this.body.canLeave && !(this.condition == null || this.condition.isAlwaysTrue());
	}
	
	// ---------- Code generation ----------
	
	@Override public void generateCode(AsmWriter writer, Frame frame) throws CodeGenerationException {
		
		String startLabel = Utilities.Label.generateLabel("for_start");
		String endLabel = Utilities.Label.generateLabel("for_end");
		
		frame = new Frame(frame);
		if (this.initializer != null && this.initializer instanceof Local) {
			frame.declare((Local)this.initializer);
		}
		frame.enter(writer);
		
		if (this.initializer != null) {
			this.initializer.generateCode(writer, frame);
		}
		writer.label(startLabel);
		if (this.condition != null) {
			this.condition.generateCode(writer, frame);
			writer.instr("cmp", "eax", 0);
			writer.instr("je", endLabel);
		}
		
		this.body.generateCode(writer, frame);
		
		if (this.postExpression != null) {
			this.postExpression.generateCode(writer, frame);
		}
		writer.instr("jmp", startLabel);
		writer.label(endLabel);
		frame.leave(writer);
	}
}
