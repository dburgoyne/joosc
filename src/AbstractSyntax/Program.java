package AbstractSyntax;

import java.util.ArrayList;
import java.util.List;

import Parser.ParseTree;
import Utilities.Cons;

public class Program extends ASTNode {
	protected List<Classfile> files;
	
	public Program(ParseTree... trees) {
		super(null);
		files = new ArrayList<Classfile>();
		for (ParseTree tree : trees) {
			Classfile file = new Classfile(tree);
			files.add(file);
		}
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException {
		for (Classfile file : files) {
			EnvironmentDecl export = file.exportEnvironmentDecls();
			assert(export != null);
			this.environment = new Cons<EnvironmentDecl>(export, this.environment);
		}
		for (Classfile file : files) {
			file.buildEnvironment(this.environment);
		}
	}

	public EnvironmentDecl exportEnvironmentDecls() {
		// Do nothing.
		return null;
	}
}
