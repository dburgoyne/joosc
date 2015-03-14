package Types;

// The type of the null literal.
public enum NullType implements Type {
	INSTANCE;

	@Override public String getCanonicalName() {
		return "(null)";
	}

	@Override public boolean canBeCastAs(Type t) {
		return !(t instanceof PrimitiveType);
	}

	@Override public boolean canBeAssignedTo(Type t) {
		return !(t instanceof PrimitiveType);
	}

}
