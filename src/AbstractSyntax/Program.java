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
		// If environment generation fails for one file, continue with the others,
		// then throw the original exception.
		NameConflictException err = null;
		for (Classfile file : files) {
			try {
				file.buildEnvironment(this.environment);
			} catch (NameConflictException caught) {
				if (err != null) {
					System.err.println(err);
				}
				err = caught;
			}
		}
		if (err != null) {
			throw err;
		}
	}

	public EnvironmentDecl exportEnvironmentDecls() {
		// Do nothing.
		return null;
	}
}
