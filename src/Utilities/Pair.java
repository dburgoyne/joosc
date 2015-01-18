/*
 * CS 444
 * Assignment 1
 * 2015-01-16
 *
 * Pair.java
 *   Class that models a typed pair, since Java apparently lacks this construct.
 *
 * AUTHORS:
 *   Danny Burgoyne UWID# 20411624 <secure@dburgoyne.ca>
 *   TODO add other contributors
 *   
 */

package Utilities;

public class Pair<L, R> {

    private final L left;
    private final R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() {
        return left;
    }
    public R getRight() {
        return right;
    }
}
