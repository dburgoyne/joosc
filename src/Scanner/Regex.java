/*
 * CS 444
 * Assignment 1
 * 2015-01-16
 *
 * Regex.java
 *   Class that models a regular expression for use in joosc's scanner.
 *
 * AUTHORS:
 *   Danny Burgoyne UWID# 20411624 <secure@dburgoyne.ca>
 *   TODO add other contributors
 *   
 */

public class Regex {

    public static enum Type {
        EMPTY_SET,
        EMPTY_STRING,
        CHARACTER,
        STRING,  // Pseudo-type, expanded to concatenation of characters on intantiation.
        CONCATENATION,
        KLEENE_CLOSURE,
        DISJUNCTION,
        CONJUNCTION,
        COMPLEMENT
    }
    
    public static boolean isNullable(Regex r) {
        boolean toReturn = false;
        
        switch(r.getType()) {
            case CHARACTER:
            case EMPTY_SET:
                break;
            case EMPTY_STRING:
            case KLEENE_CLOSURE:
                toReturn = true;
                break;
            case CONCATENATION:
            case CONJUNCTION:
                toReturn = isNullable(r.getInner1()) && isNullable(r.getInner2());
                break;
            case DISJUNCTION:
                toReturn = isNullable(r.getInner1()) || isNullable(r.getInner2());
                break;
            case COMPLEMENT:
                toReturn = !isNullable(r.getInner1());
                break;
        }
        
        return toReturn;
    }
    
    public static Regex derivative(Regex r, char a) {
        // TODO Consider simplifying before and/or after this operation
        Regex toReturn = null;
        
        switch(r.getType()) {
            case CHARACTER:
                toReturn = (a == r.getSymbol())
                           ? new Regex(Regex.Type.EMPTY_STRING)
                           : new Regex(Regex.Type.EMPTY_SET);
                break;
            case EMPTY_SET:
            case EMPTY_STRING:
                toReturn = new Regex(Regex.Type.EMPTY_SET);
                break;
            case KLEENE_CLOSURE:
                toReturn = new Regex(Regex.Type.CONCATENATION,
                                     derivative(r.getInner1(), a),
                                     r);
                break;
            case CONCATENATION:
                toReturn = isNullable(r.getInner1())
                           ? new Regex(Regex.Type.DISJUNCTION,
                                          new Regex(Regex.Type.CONCATENATION,
                                                    derivative(r.getInner1(), a),
                                                    r.getInner2()),
                                       derivative(r.getInner2(), a))
                           : new Regex(Regex.Type.CONCATENATION,
                                       derivative(r.getInner1(), a),
                                       r.getInner2());
                break;
            case CONJUNCTION:
                toReturn = new Regex(Regex.Type.CONJUNCTION,
                                        derivative(r.getInner1(), a),
                                     derivative(r.getInner2(), a));
                break;
            case DISJUNCTION:
                toReturn = new Regex(Regex.Type.DISJUNCTION,
                                        derivative(r.getInner1(), a),
                                     derivative(r.getInner2(), a));
                break;
            case COMPLEMENT:
                toReturn = new Regex(Regex.Type.COMPLEMENT,
                                        derivative(r.getInner1(), a));
                break;
        }
        
        return toReturn;
    }
    
    public static Regex derivative(Regex r, String s) {
        // TODO Rewriting this as a loop may be necessary to handle long strings.
        return s.isEmpty()
               ? r
               : derivative(derivative(r,
                                       s.substring(0, s.length() - 1)),
                            s.charAt(s.length() - 1));
    }
    
    public static Regex simplify(Regex r) {
        Regex simple1, simple2;
        loop:
        while(true) {
            switch(r.getType()) {
                case KLEENE_CLOSURE:
                    if (r.getInner1().getType() == Regex.Type.KLEENE_CLOSURE) {
                        r = r.getInner1();
                        continue loop;
                    }
                    if (r.getInner1().getType() == Regex.Type.EMPTY_STRING) {
                        r = r.getInner1();
                        continue loop;
                    }
                    if (r.getInner1().getType() == Regex.Type.EMPTY_SET) {
                        r = new Regex(Regex.Type.EMPTY_STRING);
                        continue loop;
                    }
                    break;
                case CONCATENATION:
                    if (r.getInner1().getType() == Regex.Type.EMPTY_SET
                     || r.getInner2().getType() == Regex.Type.EMPTY_SET) {
                        r = new Regex(Regex.Type.EMPTY_SET);
                        continue loop;
                    }
                    if (r.getInner1().getType() == Regex.Type.EMPTY_STRING) {
                        r = r.getInner2();
                        continue loop;
                    }
                    if (r.getInner2().getType() == Regex.Type.EMPTY_STRING) {
                        r = r.getInner1();
                        continue loop;
                    }
                    break;
                case CONJUNCTION:
                    if (r.getInner1().getType() == Regex.Type.EMPTY_SET
                     || r.getInner2().getType() == Regex.Type.EMPTY_SET) {
                        r = new Regex(Regex.Type.EMPTY_SET);
                        continue loop;
                    }
                    if (r.getInner1().getType() == Regex.Type.COMPLEMENT && r.getInner1().getInner1().getType() == Regex.Type.EMPTY_SET) {
                        r = r.getInner2();
                        continue loop;
                    }
                    if (r.getInner2().getType() == Regex.Type.COMPLEMENT && r.getInner2().getInner1().getType() == Regex.Type.EMPTY_SET) {
                        r = r.getInner1();
                        continue loop;
                    }
                    simple1 = simplify(r.getInner1());
                    simple2 = simplify(r.getInner2());
                    if (simple1.equals(simple2)) {
                        r = simple1;
                        // Could return here, since r is now in its simplest form.
                        continue loop;
                    }
                    break;
                case DISJUNCTION:
                    if (r.getInner1().getType() == Regex.Type.EMPTY_SET) {
                        r = r.getInner2();
                        continue loop;
                    }
                    if (r.getInner2().getType() == Regex.Type.EMPTY_SET) {
                        r = r.getInner1();
                        continue loop;
                    }
                    if (r.getInner1().getType() == Regex.Type.COMPLEMENT && r.getInner1().getInner1().getType() == Regex.Type.EMPTY_SET
                     || r.getInner2().getType() == Regex.Type.COMPLEMENT && r.getInner2().getInner1().getType() == Regex.Type.EMPTY_SET) {
                        r = new Regex(Regex.Type.COMPLEMENT,
                                      new Regex(Regex.Type.EMPTY_SET));
                        continue loop;
                    }
                    simple1 = simplify(r.getInner1());
                    simple2 = simplify(r.getInner2());
                    if (simple1.equals(simple2)) {
                        r = simple1;
                        // Could return here, since r is now in its simplest form.
                        continue loop;
                    }
                    break;
                case COMPLEMENT:
                    if (r.getInner1().getType() == Regex.Type.COMPLEMENT) {
                        r = r.getInner1().getInner1();
                        continue loop;
                    }
                    break;
            }
            break;
        }
        return r;
    }
    
    // These fields should be immutable once the Regex object is instantiated.
    private Regex.Type m_type;
    private char m_symbol;
    private Regex m_inner1, m_inner2;
    
    public Regex.Type getType() {
        return m_type;
    }
    public char getSymbol() {
        return m_symbol;
    }
    public Regex getInner1() {
        return m_inner1;
    }
    public Regex getInner2() {
        return m_inner2;
    }

    public Regex(String s) {
        // I would like to call the other constructors from here to avoid
        // repetition, but this is not allowed by the JLS.
        if (s.isEmpty()) {
            m_type = Regex.Type.EMPTY_STRING;
        } else if (s.length() == 1) {
            m_type = Regex.Type.CHARACTER;
            m_symbol = s.charAt(0);
        } else {
            m_type = Regex.Type.CONCATENATION;
            m_inner1 = new Regex(s.charAt(0));
            m_inner2 = new Regex(s.substring(1, s.length()));
        }
    }
    public Regex(Regex.Type type) {
        this(type, null, null);
    }
    public Regex(char symbol) {
        this(Regex.Type.CHARACTER, null, null);
        m_symbol = symbol;
    }
    public Regex(Regex.Type type, Regex inner1) {
        this(type, inner1, null);
    }
    public Regex(Regex.Type type, Regex inner1, Regex inner2) {
        m_type = type;
        m_symbol = '\0';
        m_inner1 = inner1;
        m_inner2 = inner2;
    }
    
    public String toString() {
        String toReturn = "";
        
        switch(m_type) {
            case EMPTY_SET:
                toReturn = "<EMPTY_SET>";
                break;
            case EMPTY_STRING:
                toReturn = "<EMPTY_STRING>";
                break;
            case CHARACTER:
                toReturn = "" + m_symbol;
                break;
            case KLEENE_CLOSURE:
                toReturn = "(" + m_inner1.toString() + ")*";
                break;
            case CONCATENATION:
                toReturn = m_inner1.toString() + m_inner2.toString();
                break;
            case CONJUNCTION:
                toReturn = "((" + m_inner1.toString() + ") & (" + m_inner2.toString() + "))";
                break;
            case DISJUNCTION:
                toReturn = "((" + m_inner1.toString() + ") + (" + m_inner2.toString() + "))";
                break;
            case COMPLEMENT:
                toReturn = "!(" + m_inner1.toString() + ")";
                break;
        }
        
        return toReturn;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Regex)) {
            return false;
        }
        Regex r = (Regex)o;
        // TODO This needs to account for commutativity and associativity of conjunction, discunction.
        return (this.getType() == r.getType()
             && this.getSymbol() == r.getSymbol()
             && this.getInner1() == r.getInner1()
             && this.getInner2() == r.getInner2());
    }
    
    /*public static void main(String[] args) {
        Regex r1 = new Regex(Regex.Type.KLEENE_CLOSURE,
                            new Regex(Regex.Type.DISJUNCTION,
                                      new Regex("foo"),
                                      new Regex("frak")));
                                      
        System.out.println(derivative(r1, 'f'));
        System.out.println(derivative(r1, "f"));
        
        Regex r2 = new Regex(Regex.Type.CONCATENATION,
                             new Regex(Regex.Type.KLEENE_CLOSURE,
                                       new Regex('b')),
                             new Regex('a'));
        System.out.println(derivative(r2, "ba"));
        System.out.println(simplify(derivative(r2, "baa")));
        System.out.println(isNullable(derivative(r2, "baa")));
        System.out.println(isNullable(simplify(derivative(r2, "baa"))));
    }*/
}
