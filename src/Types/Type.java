package Types;

public interface Type {
	
	// Return qualified name of this type.
	public String getCanonicalName();
	
	public boolean canBeCastAs(Type t);
	public boolean canBeAssignedTo(Type t);
	
	// Return this type's runtime identifier (TypeID). 
	public int getTypeID();
}
