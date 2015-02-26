package AbstractSyntax;

public enum Modifier {
	PUBLIC,
	PROTECTED,
	ABSTRACT,
	STATIC,
	FINAL,
	NATIVE;
	
	public static Modifier fromString(String s) {
		return Modifier.valueOf(s.toUpperCase());
	}
}
