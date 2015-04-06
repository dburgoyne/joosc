package AbstractSyntax;

import java.util.ArrayList;
import java.util.List;

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
import Utilities.BiPredicate;
import Utilities.Cons;
import Utilities.StringUtils;

public class Method extends Decl {

	protected List<Modifier> modifiers;
	protected List<Formal> parameters;
	protected Type type; // null if void !
	protected Block block; // null if non-concrete declaration!
	protected boolean isGramaticallyAbstract = false;
	protected TypeDecl declaringType;
	
	public Identifier getName() {
		return this.name;
	}
	
	public TypeDecl getDeclaringType() {
		return this.declaringType;
	}
	
	public boolean isAbstract() {
		return isGramaticallyAbstract || modifiers.contains(Modifier.ABSTRACT);
	}
	
	public boolean isStatic() {
		return modifiers.contains(Modifier.STATIC);
	}
	
	public boolean isNative() {
		return modifiers.contains(Modifier.NATIVE);
	}
	
	public boolean isFinal() {
		return modifiers.contains(Modifier.FINAL);
	}
	
	public boolean isProtected() {
		return modifiers.contains(Modifier.PROTECTED);
	}
	
	public boolean isPublic() {
		// Interface methods are default public.
		return isGramaticallyAbstract || modifiers.contains(Modifier.PUBLIC);
	}
	
	public Method(ParseTree tree, boolean isAbstract) {
		super(tree);
		assert(isAbstract 
				? tree.getSymbol().equals("AbstractMethodDeclaration")
				: tree.getSymbol().equals("MethodDeclaration")  );
		this.isGramaticallyAbstract = isAbstract;
		if (tree.getSymbol().equals("MethodDeclaration")) {
			extractMethodHeader(tree.getChildren()[0]);
			extractMethodBody(tree.getChildren()[1]);
		} else if (tree.getSymbol().equals("AbstractMethodDeclaration")) {
			if (tree.numChildren() == 3) {
				this.typeName = new Identifier(tree.getChildren()[0]);
				extractMethodDeclarator(tree.getChildren()[1]);
			} else if (tree.numChildren() == 4) {
				this.modifiers = Modifier.extractModifiers(tree.getChildren()[0]);
				this.typeName = new Identifier(tree.getChildren()[1]);
				extractMethodDeclarator(tree.getChildren()[2]);
			}
		}
	}
	
	private void extractMethodHeader(ParseTree tree) {
		assert(tree.getSymbol().equals("MethodHeader"));
		this.modifiers = Modifier.extractModifiers(tree.getChildren()[0]);
		this.typeName = new Identifier(tree.getChildren()[1]);
		extractMethodDeclarator(tree.getChildren()[2]);
	}
	
	private void extractMethodDeclarator(ParseTree tree) {
		assert(tree.getSymbol().equals("MethodDeclarator"));
		this.name = new Identifier(tree.getChildren()[0]);
		if (tree.numChildren() == 4) {
			this.parameters = extractFormalParameterList(tree.getChildren()[2]);
		} else {
			this.parameters = new ArrayList<Formal>();
		}
	}
	
	public static List<Formal> extractFormalParameterList(ParseTree tree) {
		assert(tree.getSymbol().equals("FormalParameterList"));
		List<Formal> parameters = new ArrayList<Formal>();
		while (tree.numChildren() > 1) {
			Formal f = new Formal(tree.getChildren()[2]);
			parameters.add(0, f);
			tree = tree.getChildren()[0];
		}
		Formal f = new Formal(tree.getChildren()[0]);
		parameters.add(0, f);
		return parameters;
	}
	
	private void extractMethodBody(ParseTree tree) {
		assert(tree.getSymbol().equals("MethodBody"));
		if (tree.getChildren()[0].getSymbol().equals("Block")) {
			this.block = new Block(tree.getChildren()[0]);
		} else {
			// Leave this.block null to indicate no block (semicolon).
			return;
		}
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException, ImportException {
		this.environment = parentEnvironment;
		
		// Build the environment for each formal parameter
		for (Formal formal : this.parameters) {
			formal.buildEnvironment(this.environment);
			this.environment = new Cons<EnvironmentDecl>(formal.exportEnvironmentDecls(), this.environment);
		}
		this.typeName.buildEnvironment(this.environment);
		// Build the environment for the body statements.
		if (this.block != null) {
			this.block.buildEnvironment(this.environment);
		}
	}

	public EnvironmentDecl exportEnvironmentDecls() {
		return this;
	}

	@Override
	public void linkTypes(Cons<TypeDecl> types) throws TypeLinkingException {
		
		if (!(typeName.isSimple()
				&& typeName.getSingleComponent().equals("void"))) {
			this.type = this.typeName.resolveType(types, this.environment);
		}
		
		for (Formal formal : parameters) {
			formal.linkTypes(types);
		}
		if (block != null) {
			block.linkTypes(types);
		}
	}

	@Override
	public void linkNames(TypeDecl curType, boolean staticCtx, EnvironmentDecl curDecl, Local curLocal, boolean lValue) throws NameLinkingException {
		this.declaringType = curType;
		for (Formal formal : parameters) {
			formal.linkNames(curType, staticCtx, this, curLocal, false);
		}
		if (block != null) {
			block.linkNames(curType, staticCtx, this, curLocal, false);
		}
	}
	
	public static class SameSignaturePredicate implements BiPredicate<Method> {
		public boolean test(Method m1, Method m2) {
			if (!new Equality<Identifier>().test(m1.name, m2.name)) return false;
			if (m1.parameters.size() != m2.parameters.size()) return false;
			
			for (int i = 0; i < m1.parameters.size(); i++) {
				if (!       m1.parameters.get(i).type
				    .equals(m2.parameters.get(i).type))
					return false;
			}
			
			return true;
		}
	}

	public static class SameVisibilityPredicate implements BiPredicate<Method> {
		public boolean test(Method m1, Method m2) {
			return m1.isProtected() && m2.isProtected()
				|| m1.isPublic()    && m2.isPublic();
		}
	}
	
	public static class NarrowerVisibilityThanPredicate implements BiPredicate<Method> {
		// Returns true iff m2 has wider visibility than m1.
		public boolean test(Method m1, Method m2) {
			return m1.isProtected() && m2.isPublic();
		}
	}
	
	public static class SameAbstractnessPredicate implements BiPredicate<Method> {
		public boolean test(Method m1, Method m2) {
			return m1.isAbstract() == m2.isAbstract();
		}
	}
	
	public static class SameReturnTypePredicate implements BiPredicate<Method> {
		public boolean test(Method m1, Method m2) {
			return new Equality<Type>().test(m1.type, m2.type);
		}
	}
	
	public static class SameSignatureSameReturnTypePredicate implements BiPredicate<Method> {
		public boolean test(Method m1, Method m2) {
			return new SameSignaturePredicate().test(m1, m2)
			    && new SameReturnTypePredicate().test(m1, m2);
		}
	}
	
	public static class SameSignatureSameReturnTypeSameVisibilityPredicate implements BiPredicate<Method> {
		public boolean test(Method m1, Method m2) {
			return new SameSignaturePredicate().test(m1, m2)
				&& new SameReturnTypePredicate().test(m1, m2)
		    	&& new SameVisibilityPredicate().test(m1, m2);
		}
	}

	public static class SameSignatureDifferentReturnTypePredicate implements BiPredicate<Method> {
		public boolean test(Method m1, Method m2) {
			return  new SameSignaturePredicate().test(m1, m2)
			    && !new SameReturnTypePredicate().test(m1, m2);
		}
	}
	
	public static class SameSignatureDifferentVisibilityPredicate implements BiPredicate<Method> {
		public boolean test(Method m1, Method m2) {
			return  new SameSignaturePredicate().test(m1, m2)
			    && !new SameVisibilityPredicate().test(m1, m2);
		}
	}
	
	public static class SameSignatureNarrowerVisibilityThanDifferentAbstractnessPredicate implements BiPredicate<Method> {
		public boolean test(Method m1, Method m2) {
			return  new SameSignaturePredicate().test(m1, m2)
			    &&  new NarrowerVisibilityThanPredicate().test(m1, m2)
			    && !new SameAbstractnessPredicate().test(m1, m2);
		}
	}
	
	public static class SameSignatureStaticPredicate implements BiPredicate<Method> {
		public boolean test(Method m1, Method m2) {
			return  new SameSignaturePredicate().test(m1, m2)
			    && m2.isStatic();
		}
	}
	
	public static class SameSignatureNonStaticPredicate implements BiPredicate<Method> {
		public boolean test(Method m1, Method m2) {
			return  new SameSignaturePredicate().test(m1, m2)
			    && !m2.isStatic();
		}
	}
	
	public static class SameSignatureFinalPredicate implements BiPredicate<Method> {
		public boolean test(Method m1, Method m2) {
			return new SameSignaturePredicate().test(m1, m2)
			    && m2.isFinal();
		}
	}
	
	public static class SameSignaturePublicPredicate implements BiPredicate<Method> {
		public boolean test(Method m1, Method m2) {
			return new SameSignaturePredicate().test(m1, m2)
			    && m2.isPublic();
		}
	}
	
	public String toString() {
		return (this.type == null ? "void" : this.type) + " "
				+ this.getName() + "("
				+ StringUtils.join(this.parameters, ", ")
				+ ")";
		}

	@Override
	public void checkTypes() throws TypeCheckingException {
		for (Formal formal : parameters) {
			formal.checkTypes();
		}
		if (block != null) {
			block.checkTypes();
		}
	}
	
	@Override public void checkReachability(boolean canLeavePrevious) throws ReachabilityException {
		if (this.block != null) {
			this.block.checkReachability(true);
		}
		
		// If we can reach the end of the block, and our return type is non-void,
		// then throw.
		if (this.block != null && this.block.canLeave && this.type != null) {
			throw new ReachabilityException.MayNotReturn(this);
		}
	}
	
	// ---------- Code generation ----------
	
	public String getDispatcherLabel() {
		return Utilities.Label.generateLabel(this.isStatic() ? "sm" : "call",
				this.declaringType.getCanonicalName(),
				this.getName().getSingleComponent(),
				Utilities.Label.typesOfFormals(this.parameters));
	}
	
	public String getImplementationLabel() {
		return Utilities.Label.generateLabel(this.isStatic() ? "sm" : "im",
				this.declaringType.getCanonicalName(),
				this.getName().getSingleComponent(),
				Utilities.Label.typesOfFormals(this.parameters));
	}

	@Override public void generateCode(AsmWriter writer, Frame frame) throws CodeGenerationException {
		writer.pushComment("Method %s", this);

		String implLbl = this.getImplementationLabel();
		writer.verbatimfn("global %s", implLbl);
		writer.label(implLbl);
		writer.justDefinedGlobal(implLbl);
		
		// New top-level frame.
		frame = new Frame();
		frame.declare(this.parameters, this.isStatic());
		frame.enter(writer);

		if (this.block == null) {
			if (this.isNative()) {  // Special case for java.io.OutputStream.nativeWrite(int)
				String label = "NATIVE" + this.getDeclaringType().getCanonicalName() + "." + this.name.getSingleComponent();
				writer.instr("mov", "eax", frame.deref(this.parameters.get(0)));
				writer.instr("call", label);
				writer.justUsedGlobal(label);
			} else {
				writer.comment("No code for abstract method %s", this.name);
			}
		} else {
			this.block.generateCode(writer, frame);
		}
		frame.leave(writer);
		// Add one for 'this' pointer.
		writer.instr("ret", (this.parameters.size() + (this.isStatic() ? 0 : 1)) * 4);
		
		if (!this.isStatic()) {
			writer.comment("Method %s dispatcher", this);

			String callLbl = this.getDispatcherLabel();
			writer.verbatimfn("global %s", callLbl);
			writer.label(callLbl);
			writer.justDefinedGlobal(callLbl);
			
			// Can't call methods on a null object.
			writer.instr("mov", "eax", "[esp + " + 4*(this.parameters.size() + 1) + "]"); // eax <- this
			writer.instr("cmp", "eax", 0);
			writer.instr("je",    "__exception");
			writer.justUsedGlobal("__exception");
			
			writer.instr("mov", "eax", "[eax]"); // eax <- this.tid
			writer.instr("mov", "eax",           // eax <- V_(T, S)
					"[eax*4 + " + this.declaringType.getSubtypeTableLabel() + "]");
			writer.instr("jmp",                  // direct jump to impl
					"[eax + " + 4*this.getVtableIndex() + "]");
		}
		
		writer.popComment();
		
		if (this == Program.staticIntTest) {
			Program.generateStart(writer);
		}
	}
	
	// Search self in vtable of this.declaringType using the index of this method in
	// this.declaringType.allInstanceMethods. 
	private int getVtableIndex() {
		Method[] vtable = this.declaringType.getAllInstanceMethods();
		int index;
		for (index = 0; index < vtable.length; index++) {
			if (vtable[index] == this) break;
		}
		assert index < vtable.length;
		return index;
	}
}
