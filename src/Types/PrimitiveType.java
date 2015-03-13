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
	
	@Override public boolean canCastTo(Type t) {
		return t instanceof PrimitiveType
		    && (this.isIntegral() == ((PrimitiveType)t).isIntegral());
	}
	
	@Override public boolean canAssignTo(Type t) {
		return t instanceof PrimitiveType
			&& (this.isIntegral() == ((PrimitiveType)t).isIntegral())
			&& (this.width() <= ((PrimitiveType)t).width());
	}
	
	private int width() {
		return this == BOOLEAN ? 1
			 : this == BYTE ? 8
			 : this == CHAR ? 16
			 : this == SHORT ? 16
			 : 32;
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
