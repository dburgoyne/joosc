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
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) {
		for (Classfile file : files) {
			List<EnvironmentDecl> exports = file.exportEnvironmentDecls();
			this.environment = this.environment.append(exports);
		}
	}

	public List<EnvironmentDecl> exportEnvironmentDecls() {
		// Do nothing.
		return null;
	}
}
