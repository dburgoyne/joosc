package AbstractSyntax;

import java.util.List;

public class Classfile extends ASTNode {

	protected Identifier packageName;
	protected List<Identifier> imports;
	protected TypeDecl typeDecl;
	
	
}
