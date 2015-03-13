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
 *   Uros Dimitrijevic UWID# 20373732 <udimitri@uwaterloo.ca>
 *   Xiang Fang <x2fang@uwaterloo.ca>
 *   
 */

package Scanner;

public class Regex {

    public static enum Type {
        // TODO Consider adding support for character equivalence classes.
        // Idea: change the m_symbol field in this class to a predicate that returns true iff it is fed
        // a character in the class.
        EMPTY_SET,
        EMPTY_STRING,
        CHARACTER,
        CHARACTER_CLASS,
        STRING,  // Pseudo-type, expanded to concatenation of characters on instantiation.
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
            case CHARACTER_CLASS:
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
            default: break;
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
            case CHARACTER_CLASS:
                toReturn = (r.getCharacterClass().matches(a))
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
            default: break;
        }
        
        return toReturn;
    }

	public static Regex derivative(Regex r, String s) {
        for (int i = 0; i < s.length(); i++) {
            r = derivative(r, s.charAt(i));
        }
        return r;
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
                    // TODO Maybe add the simplification (complement(empty set))^* ~ complement(empty set)
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
                default: break;
            }
            break;
        }
        return r;
    }
    
    // These fields should be immutable once the Regex object is instantiated.
    private Regex.Type m_type;
    private char m_symbol;
    private CharacterClass m_characterClass;
    private Regex m_inner1, m_inner2;
    
    public Regex.Type getType() {
        return m_type;
    }
    public char getSymbol() {
        return m_symbol;
    }
    public CharacterClass getCharacterClass() {
		return m_characterClass;
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
    public Regex(CharacterClass characterClass) {
        this(Regex.Type.CHARACTER_CLASS, null, null);
        m_characterClass = characterClass;
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
                //toReturn = "<EMPTY_SET>";
                break;
            case EMPTY_STRING:
                //toReturn = "<EMPTY_STRING>";
                break;
            case CHARACTER:
                toReturn = "" + m_symbol;
                break;
            case CHARACTER_CLASS:
                //toReturn = "<CHARACTER_CLASS>";
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
            default: break;
        }
        
        return toReturn;
    }
    
    public boolean isEmptySet() {
        boolean toReturn = false;
        
        switch(m_type) {
            case EMPTY_SET:
                toReturn = true;
                break;
            case KLEENE_CLOSURE:
                toReturn = m_inner1.isEmptySet();
                break;
            case CONCATENATION:
            case CONJUNCTION:
                toReturn = m_inner1.isEmptySet() || m_inner2.isEmptySet();
                break;
            case DISJUNCTION:
                toReturn = m_inner1.isEmptySet() && m_inner2.isEmptySet();
                break;
            case COMPLEMENT:
                toReturn = !m_inner1.isEmptySet();
                break;
            default: break;
        }
        
        return toReturn;
    }
    
    // Convenience method for building large disjunctions/concatenations.
    public static Regex Build(Regex.Type type, Regex... args) {
        if (args.length == 0) {
            return new Regex(Regex.Type.EMPTY_SET);
        }
        Regex toReturn = args[args.length - 1];
        for (int i = args.length - 2; i >= 0; i--) {
            toReturn = new Regex(type,
                                 args[i],
                                 toReturn);
        }
        return toReturn;
    }
    
    // Convenience method for handling optional RHS symbols in JLS lexical productions.
    public static Regex Optional(Regex r) {
        return new Regex(Regex.Type.DISJUNCTION,
        		new Regex(Regex.Type.EMPTY_STRING),
        		r);
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
        // TODO This may need to account for character class equivalence.
        return (this.getType() == r.getType()
             && this.getSymbol() == r.getSymbol()
             && this.getInner1() == r.getInner1()
             && this.getInner2() == r.getInner2());
    }
    
    public static void main(String[] args) {
        Regex r1 = new Regex(Regex.Type.COMPLEMENT,
                             new Regex(Regex.Type.EMPTY_SET));
                                      
        System.out.println(isNullable(derivative(r1, "herpderp")));
    }
}
