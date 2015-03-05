package Utilities;

public interface BiPredicate<T> {
	boolean test(T t1, T t2);
	
	public static final class Equality implements BiPredicate<Object> {
		public boolean test(Object t1, Object t2) {
			return t1 == null ? t2 == null : t1.equals(t2);
		}
	}
}
