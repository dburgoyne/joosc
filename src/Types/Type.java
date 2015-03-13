package Types;

public interface Type {
	
	// Return qualified name of this type.
	public String getCanonicalName();
	
	public boolean canCastTo(Type t);
	
}
