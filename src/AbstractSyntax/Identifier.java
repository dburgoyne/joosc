package AbstractSyntax;

import java.util.List;

public class Identifier {

	protected List<String> components;
	
	// Convenience functions.
	protected boolean isArray() {
		return components.get(components.size() - 1).equals("[]");
	}
	
	protected boolean isStarImport() {
		return components.get(components.size() - 1).equals("*");
	}
}
