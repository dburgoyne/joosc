package Types;

public enum PrimitiveType implements Type {
	BOOLEAN("boolean"),
	BYTE("byte"),
	CHAR("char"),
	SHORT("short"),
	INT("int");

	final String canonicalName;
	private PrimitiveType(String canonicalName) {
		this.canonicalName = canonicalName;
	}
	
	public boolean isIntegral() {
		return (this != BOOLEAN);
	}
	
	@Override public boolean canBeCastAs(Type t) {
		return t instanceof PrimitiveType
		    && (this.isIntegral() == ((PrimitiveType)t).isIntegral());
	}
	
	@Override public boolean canBeAssignedTo(Type t) {
		return t instanceof PrimitiveType
			&& (this == BOOLEAN ? t == BOOLEAN
			: this == BYTE ? (t == BYTE || t == SHORT || t == INT)
			: this == CHAR ? (t == CHAR || t == INT)
			: this == SHORT ? (t == SHORT || t == INT)
			: (t == INT));
	}
	
	
	@Override public String getCanonicalName() {
		return canonicalName;
	}

	public static PrimitiveType fromString(String x) {
		for (PrimitiveType prim : PrimitiveType.values()) {
			if (prim.getCanonicalName().equals(x)) {
				return prim;
			}
		}
		return null;
	}
}
