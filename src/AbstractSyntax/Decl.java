package AbstractSyntax;

import Parser.ParseTree;
import Utilities.BiPredicate;
import Utilities.Predicate;

public abstract class Decl extends ASTNode implements EnvironmentDecl {

	protected Identifier typeName;
	protected Identifier name;
	
	public Decl(ParseTree tree, Identifier type, Identifier name) {
		super(tree);
		this.typeName = type;
		this.name = name;
	}
	
	public Decl(ParseTree tree) {
		super(tree);
	}

	public static class SameNamePredicate implements BiPredicate<EnvironmentDecl> {
		@Override public boolean test(EnvironmentDecl t1, EnvironmentDecl t2) {
			return new BiPredicate.Equality<Identifier>()
						.test(t1.getName(), t2.getName());
		}
	}
	
	public static class HasNamePredicate implements Predicate<EnvironmentDecl> {
		public HasNamePredicate(Identifier name) { this.name = name; }
		public Identifier name;
		@Override public boolean test(EnvironmentDecl t) {
			return t.getName().equals(this.name);
		}
	}
}
