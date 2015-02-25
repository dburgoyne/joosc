package AbstractSyntax;

import java.util.List;

public class Method extends Decl {

	protected List<Modifier> modifiers;
	protected List<Formal> parameters;
	
	protected Block statements;
}
