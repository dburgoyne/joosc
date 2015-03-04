package Types;

import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;

import Utilities.Cons;
import AbstractSyntax.TypeDecl;

/// An acyclic directed graph of all sub-type relations.
public class Hierarchy {
	protected final TypeDecl[]                 types;   // index -> TypeDecl map
	protected final HashMap<TypeDecl, Integer> indices; // TypeDecl -> index map
	public int numTypes() { return this.types.length; }
	
	/// index i <: index j adjacency matrix.
	/// Entry (i,j) is at matrix[numTypes()*i + j]. 
	protected final BitSet matrix;
	
	public Hierarchy(Cons<TypeDecl> allTypeDecls) {
		this.types = Cons.toList(allTypeDecls).toArray(new TypeDecl[0]);
		this.matrix = new BitSet(this.numTypes() * this.numTypes());
		
		this.indices = new HashMap<TypeDecl, Integer>();
		for (int i = 0; i < numTypes(); i++) {
			this.indices.put(types[i], i);
		}
	}
	
	public void buildDAG() {
		BitSet visited = new BitSet(numTypes());
		
		for (int i = 0; i < numTypes(); i++) {
			if (visited.get(i)) continue;
			
			for (TypeDecl parent : types[i].getDirectSupertypes()) {
				
			}
		}
	}
	
	public abstract static class Exception extends java.lang.Exception {

		private static final long serialVersionUID = -1894443252407413173L;
		
		
	}
}
