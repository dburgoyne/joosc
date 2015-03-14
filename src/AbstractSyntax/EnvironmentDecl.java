package AbstractSyntax;

// Anything that could appear in an environment (class, interface, field,
// method, local variable, formal parameter).
public interface EnvironmentDecl {
	public Identifier getName();
	public String getPositionalString();
}
