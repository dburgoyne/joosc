package Utilities;

public class PredUtils {

	public static abstract class Chain<T> implements Predicate<T> {
		public Chain<T> and(final Predicate<? super T> q) {
			final Predicate<T> p = this;
			return new Chain<T>() {
				@Override public boolean test(T t) {
					return p.test(t) && q.test(t);
				}
			};
		}
		
		public <U extends T> Chain<U> and_(final Predicate<U> q) {
			final Predicate<T> p = this;
			return new Chain<U>() {
				@Override public boolean test(U t) {
					return p.test(t) && q.test(t);
				}
			};
		}

		public Chain<T> or(final Predicate<? super T> q) {
			final Predicate<T> p = this;
			return new Chain<T>() {
				@Override public boolean test(T t) {
					return p.test(t) || q.test(t);
				}
			};
		}
		
		public <U extends T> Chain<U> or_(final Predicate<U> q) {
			final Predicate<T> p = this;
			return new Chain<U>() {
				@Override public boolean test(U t) {
					return p.test(t) || q.test(t);
				}
			};
		}
		
		public Chain<T> not() {
			final Predicate<T> p = this;
			return new Chain<T>() {
				@Override public boolean test(T t) {
					return !p.test(t);
				}
			};
		}
	}

	public static<T> Chain<T> both(final Predicate<T> p) {
		return new Chain<T>() {
			@Override public boolean test(T t) {
				return p.test(t);
			}
		};
	}

	public static<T> Chain<T> either(final Predicate<T> p) {
		return both(p);
	}
	
	public static<T> Chain<T> not(final Predicate<T> p) {
		return both(p).not();
	}
	
	public static <T> Predicate<T> isa(final Class<?> klass) {
		return new Predicate<T>() {
			@Override public boolean test(T t) {
				return klass.isInstance(t);
			}
		};
	}
}
