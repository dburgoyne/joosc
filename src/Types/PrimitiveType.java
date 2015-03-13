package Types;

public enum PrimitiveType implements Type {
	BOOLEAN("boolean"),
	BYTE("byte"),
	INT("int"),
	SHORT("short"),
	CHAR("char");

	final String canonicalName;
	private PrimitiveType(String canonicalName) {
		this.canonicalName = canonicalName;
	}
	
	public boolean isIntegral() {
		return (this != BOOLEAN);
	}
	
	@Override public boolean canCastTo(Type t) {
		return (t instanceof PrimitiveType && (this.isIntegral() == ((PrimitiveType)t).isIntegral()));
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
