package AbstractSyntax;

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
}
