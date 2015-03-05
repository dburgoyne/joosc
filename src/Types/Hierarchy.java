package Types;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import Utilities.Cons;
import AbstractSyntax.TypeDecl;

/// An acyclic directed graph of all sub-type relations.
public class Hierarchy {
	
	protected final TypeDecl[]                 types;   // index -> TypeDecl map
	protected final HashMap<TypeDecl, Integer> indices; // TypeDecl -> index map
	public int numTypes() { return this.types.length; }
	
	protected final Graph graph;
	protected class Graph {
		/// index i <: index j adjacency matrix.
		/// Entry (i,j) is at matrix[numTypes()*i + j]. 
		protected final BitSet matrix;
		public Graph() {
			this.matrix = new BitSet(numTypes() * numTypes());
			
			// Graph reflects type hierarchy
			for (int i = 0; i < numTypes(); i++) {
				for (TypeDecl parent : types[i].getDirectSupertypes()) {
					setSubtype(types[i], parent, true);
				}
			}
		}
		
		// Graph Accessors:
		public boolean isSubtype(int i, int j) {
			assert 0 <= i && i < numTypes();
			assert 0 <= j && j < numTypes();
			return matrix.get(numTypes()*i + j);
		}
		public boolean isSubtype(TypeDecl i, TypeDecl j) {
			return isSubtype(indices.get(i), indices.get(j));
		}
		public void setSubtype(int i, int j, boolean flag) {
			assert 0 <= i && i < numTypes();
			assert 0 <= j && j < numTypes();
			matrix.set(numTypes()*i + j, flag);
		}
		public void setSubtype(TypeDecl i, TypeDecl j, boolean flag) {
			setSubtype(indices.get(i), indices.get(j), flag);
		}
		
		private final int NO_MARK = 0, TEMP_MARK = 1, PERM_MARK = 2;
		
		public List<Integer> topologicalSort() throws CycleDetected {
			
			// Resulting list.
			LinkedList<Integer> sorted = new LinkedList<Integer>();
			
			// Map of: index of type -> its mark-status (color) 
			int[] marks = new int[numTypes()]; 
			int lastIx = 0; // index 
			
			for (;;) {
				
				// Look for an unmarked node.
				int unmarkedIx = -1;
				for (int off = 0; off < numTypes(); off++) {
					if (marks[(lastIx + off) % numTypes()] == NO_MARK) {
						unmarkedIx = (lastIx + off) % numTypes();
						break;
					}
				}
				
			  if (unmarkedIx == -1) break; // If none, done.
				
			    // Else, found one.
			    lastIx = unmarkedIx;
			    
			    visit(sorted, marks, unmarkedIx); // Visit it.
			}
			
			return sorted;
		}
		
		private void visit(LinkedList<Integer> sorted, int[] marks, int toVisit) 
				throws CycleDetected {
			
			if (marks[toVisit] == TEMP_MARK) {
				throw new CycleDetected(types[toVisit]);
			}
			
			if (marks[toVisit] == NO_MARK) {
				marks[toVisit] = TEMP_MARK;
				
				// Visit toVisit's parents.
				for (TypeDecl parent : types[toVisit].getDirectSupertypes()) {
					visit(sorted, marks, indices.get(parent));
				}
				
				marks[toVisit] = PERM_MARK;
				sorted.add(toVisit);
			}
		}
	}
	
	public Hierarchy(Cons<TypeDecl> allTypeDecls) {
		this.types = Cons.toList(allTypeDecls).toArray(new TypeDecl[0]);
		this.indices = new HashMap<TypeDecl, Integer>();
		for (int i = 0; i < numTypes(); i++) {
			this.indices.put(types[i], i);
		}
		
		this.graph = new Graph();
	}
	
	public List<TypeDecl> topologicalSort() throws CycleDetected {
		List<Integer> ixs = graph.topologicalSort();
		List<TypeDecl> sortedTypes = new ArrayList<TypeDecl>(ixs.size());
		for (int ix : ixs) {
			sortedTypes.add(types[ix]);
		}
		return sortedTypes;
	}
	
	public static class CycleDetected extends java.lang.Exception {

		private static final long serialVersionUID = -1894443252407413173L;
		
		public CycleDetected(TypeDecl ty) {
			super(String.format("Cycle detected in the inheritance hierarchy"
							  + " of type %s.\n at %s.",
					ty.getCanonicalName(),
					ty.getPositionalString()));
		}
	}
}
