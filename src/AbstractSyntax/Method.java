package AbstractSyntax;

import java.util.ArrayList;
import java.util.List;

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
	
	public Identifier getName() {
		return this.name;
	}
	
	public boolean isAbstract() {
		return isGramaticallyAbstract || modifiers.contains(Modifier.ABSTRACT);
	}
	
	public boolean isStatic() {
		return modifiers.contains(Modifier.STATIC);
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
	
	public static class SameSignaturePredicate implements BiPredicate<Method> {
		public boolean test(Method m1, Method m2) {
			if (!new Equality().test(m1.name, m2.name)) return false;
			if (m1.parameters.size() != m2.parameters.size()) return false;
			
			for (int i = 0; i < m1.parameters.size(); i++) {
				if (!       m1.parameters.get(i).type
				    .equals(m2.parameters.get(i).type))
					return false;
			}
			
			return true;
		}
	}
	
	public static class SameReturnTypePredicate implements BiPredicate<Method> {
		public boolean test(Method m1, Method m2) {
			return new Equality().test(m1.type, m2.type);
		}
	}
	
	public static class SameSignatureSameReturnTypePredicate implements BiPredicate<Method> {
		public boolean test(Method m1, Method m2) {
			return new SameSignaturePredicate().test(m1, m2)
			    && new SameReturnTypePredicate().test(m1, m2);
		}
	}
	
	public static class SameSignatureDifferentReturnTypePredicate implements BiPredicate<Method> {
		public boolean test(Method m1, Method m2) {
			return  new SameSignaturePredicate().test(m1, m2)
			    && !new SameReturnTypePredicate().test(m1, m2);
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
		return this.type + " "
				+ this.getName() + "("
				+ StringUtils.join(this.parameters, ", ")
				+ ")";
		}
}
