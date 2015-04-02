package Types;

public class ArrayType implements Type {
	final Type type;
	public Type getInnerType() { return type; }
	
	public ArrayType(Type type) {
		if (type instanceof ArrayType) {
			String err = 
				"Attempted to create array of " + type.getCanonicalName();
			throw new UnsupportedOperationException(err);
		}
		this.type = type;
		assert this.type != null;
	}

	@Override public String getCanonicalName() {
		return this.type.getCanonicalName() + "[]";
	}
	
	@Override public String toString() {
		return this.getCanonicalName();
	}
	
	@Override public int getTypeID() {
		return 0;
	}
	
	@Override public boolean canBeCastAs(Type t) {
		return t.canBeAssignedTo(this) || this.canBeAssignedTo(t);
	}
	
	@Override public boolean canBeAssignedTo(Type t) {
		boolean samePrimitiveType = (t instanceof ArrayType
				&& this.getInnerType() instanceof PrimitiveType
				&& this.getInnerType() == ((ArrayType)t).getInnerType());
		boolean compatibleReferenceTypes = (t instanceof ArrayType
				&& !(this.getInnerType() instanceof PrimitiveType)
				&& this.getInnerType().canBeAssignedTo(((ArrayType)t).getInnerType()));
		return (t.getCanonicalName().equals("java.lang.Object")
				 || t.getCanonicalName().equals("java.io.Serializable")
				 || t.getCanonicalName().equals("java.lang.Cloneable")
				 || samePrimitiveType
				 || compatibleReferenceTypes);
	}
	
	public int hashCode() {
		return type.hashCode();
	}
	
	public boolean equals(Object other) {
		if (other == null) return false;
		if (!(other instanceof ArrayType)) return false;
		return ((ArrayType)other).type.equals(type);
	}
}
