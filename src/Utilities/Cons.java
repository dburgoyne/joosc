package Utilities;

import java.util.ArrayList;
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
	
	public static<T> List<T> toList(Cons<T> cons) {
		List<T> list = new ArrayList<T>();
		while (cons != null) {
			list.add(cons.head);
			cons = cons.tail;
		}
		
		return list;
	}
	
	public static<T> Cons<T> fromList(List<T> list) {
		Cons<T> cons = null;
		for(int i = list.size()-1; i >= 0; i--) {
			cons = new Cons<T>(list.get(i), cons);
		}
		
		return cons;
	}
	
	public static <T> boolean contains(Cons<T> cons, T target) {
		if (cons == null) {
			return false;
		}
		if (cons.head == null ? target == null : cons.head.equals(target)) {
			return true;
		}
		return contains(cons.tail, target);
	}
	
	public static <T> boolean contains(Cons<T> cons, T target, BiPredicate<T> cmp) {
		while (cons != null) {
			if (cmp.test(target, cons.head)) return true;
		}
		return false;
	}
	
	/** Takes two assumed-duplicate-free lists, and produces their concatenation
	 *  in order, with the elements of the first removed if they occur in the
	 *  second list. Duplicates are detected using the given comparator. */
	public static <T> Cons<T> union(Cons<T> list1, Cons<T> list2, BiPredicate<T> cmp) {
		Cons<T> rflist1 = null;
		
		while (list1 != null) {
			if (!contains(list2, list1.head, cmp)) {
				rflist1 = new Cons<T>(list1.head, rflist1);
			}
			list1 = list1.tail;
		}
		
		while (rflist1 != null) {
			list2 = new Cons<T>(rflist1.head, list2);
			rflist1 = rflist1.tail;
		}
		
		return list2;
	}
}
