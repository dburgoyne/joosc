package Utilities;

import java.util.List;

import AbstractSyntax.EnvironmentDecl;

public class Cons<T> {

	public T head;
	public Cons<T> tail;
	
	public Cons(T head, Cons<T> tail) {
		this.head = head;
		this.tail = tail;
	}
	
	public Cons<T> append(List<T> list) {
		Cons<T> toReturn = this;
		for (T t : list) {
			toReturn = new Cons<T>(t, toReturn);
		}
		return toReturn;
	}
}
