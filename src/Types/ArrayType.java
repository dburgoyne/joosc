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
	
	public int hashCode() {
		return type.hashCode();
	}
	
	public boolean equals(Object other) {
		if (other == null) return false;
		if (!(other instanceof ArrayType)) return false;
		return ((ArrayType)other).type.equals(type);
	}
}
