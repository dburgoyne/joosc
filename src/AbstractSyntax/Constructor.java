package AbstractSyntax;

import java.util.List;

public class Constructor extends ASTNode {
	protected Modifier modifier;
	protected List<Formal> parameters;
	protected List<BlockStatement> statements;
}
