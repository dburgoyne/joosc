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
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Pair<?,?> other = (Pair<?,?>) obj;
        if (left != other.left)
            return false;
        if (right != other.right)
            return false;
        return true;
    }
    
    @Override
    public int hashCode() {
        return (this.left.hashCode()*711) ^ this.right.hashCode();
    }
}
