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
	
	@Override public boolean canCastTo(Type t) {
		return (t.getCanonicalName().equals("java.lang.Object")
			 || t.getCanonicalName().equals("java.io.Serializable")
			 || t.getCanonicalName().equals("java.lang.Cloneable")
			 || (t instanceof ArrayType && (this.getInnerType().canCastTo(((ArrayType)t).getInnerType()))));
	}
	
	@Override public boolean canAssignTo(Type t) {
		return t instanceof ArrayType && this.getInnerType().equals(((ArrayType)t).getInnerType());
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
