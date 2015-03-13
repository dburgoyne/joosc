package Types;

// The type of the null literal.
public enum NullType implements Type {
	INSTANCE;

	@Override public String getCanonicalName() {
		return "(null)";
	}

	@Override public boolean canCastTo(Type t) {
		return !(t instanceof PrimitiveType);
	}

	@Override public boolean canAssignTo(Type t) {
		return !(t instanceof PrimitiveType);
	}

}
