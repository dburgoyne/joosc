package Utilities;

import java.util.List;

public class Cons<T> {

	public T head;
	public Cons<T> tail;
	
	public Cons(T head, Cons<T> tail) {
		this.head = head;
		this.tail = tail;
	}
	
	public static<T> Cons<T> filter(Cons<T> toFilter, Predicate<T> p) {
		if (toFilter == null) {
			return null;
		}
		if (p.test(toFilter.head)) {
			return new Cons<T>(toFilter.head, Cons.filter(toFilter.tail, p));
		}
		else { // if (!p.test(toFilter.head)) {
			return Cons.filter(toFilter.tail, p);
		}
	}
}
