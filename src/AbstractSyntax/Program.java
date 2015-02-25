package AbstractSyntax;

import java.util.List;

import Parser.ParseTree;

public class Program extends ASTNode {
	protected List<Classfile> files;
	
	public Program(ParseTree... trees) {
		super(null);
		for (ParseTree tree : trees) {
			Classfile file = new Classfile(tree);
			files.add(file);
		}
	}
}
