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
	
	// How many bytes are needed to represent this type?
	public int width() {
		return this == BOOLEAN ? 1
			 : this == BYTE ? 1
			 : this == CHAR ? 2
			 : this == SHORT ? 2
			 : 4;
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
	
	@Override public int getTypeID() {
		return this == BOOLEAN ? -1
			 : this == BYTE ? -2
			 : this == CHAR ? -3
			 : this == SHORT ? -4
			 : -5;
	}
	
	
	@Override public String getCanonicalName() {
		return canonicalName;
	}
	
	@Override public String toString() {
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
