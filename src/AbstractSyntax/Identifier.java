package AbstractSyntax;

import java.util.ArrayList;
import java.util.List;

import Parser.ParseTree;
import Utilities.StringUtils;

public class Identifier extends Expression {

	protected List<String> components;
	
	// Equals and hashCode
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (!(obj instanceof Identifier)) return false;
		Identifier other = (Identifier)obj;
		return this.components.equals(other.components);
	}
	
	@Override
	public int hashCode() {
		return this.components.hashCode();
	}
	
	public String toString() {
		return StringUtils.join(components, ".");
	}
	
	// Convenience functions.
	protected boolean isArray() {
		return this.components.get(this.components.size() - 1).equals("[]");
	}
	
	protected boolean isStarImport() {
		return this.components.get(this.components.size() - 1).equals("*");
	}
	
	protected List<String> getComponents() {
		return this.components;
	}
	
	protected String getSingleComponent() {
		assert(this.components.size() == 1);
		return this.components.get(0);
	}
	
	protected List<String> getPackageName() {
		List<String> packageName = new ArrayList<String>();
		for (int i = 0; i < this.components.size() - 1; i++) {
			packageName.add(this.components.get(i));
		}
		return packageName;
	}
	
	protected String getLastComponent() {
		return this.components.get(this.components.size() - 1);
	}
	
	// Flattens almost anything.
	public Identifier(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("AmbiguousName")
			|| tree.getSymbol().equals("PackageName")
			|| tree.getSymbol().equals("Type")
			|| tree.getSymbol().equals("ReferenceType")
			|| tree.getSymbol().equals("ArrayType")
			|| tree.getSymbol().equals("PrimitiveType")
			|| tree.getSymbol().equals("Expression")
			|| tree.isTerminal());
		components = new ArrayList<String>();
		
		// For Expressions, walk down the left spine until we hit the AmbiguousName.
		if (tree.getSymbol().equals("Expression")) {
			while (!tree.getSymbol().equals("AmbiguousName")) {
				tree = tree.getChildren()[0];
			}
		}
		
		// Type has a different grammatical structure than the others.
		if (tree.getSymbol().equals("Type")) {
			extractType(tree);
		} else if (tree.getSymbol().equals("ReferenceType")) {
			extractReferenceType(tree);
		} else if (tree.getSymbol().equals("ArrayType")) {
			extractArrayType(tree);
		} else if (tree.getSymbol().equals("PrimitiveType")) {
			extractPrimitiveType(tree);
		} else if (tree.getSymbol().equals("AmbiguousName") || tree.getSymbol().equals("PackageName")) {
			extractAmbiguousOrPackageName(tree);
		} else if (tree.isTerminal()) {
			components.add(0, tree.getSymbol());
		}
	}
	
	private void extractAmbiguousOrPackageName(ParseTree tree) {
		assert(tree.getSymbol().equals("AmbiguousName") || tree.getSymbol().equals("PackageName"));
		
		while (tree.numChildren() > 1) {
			components.add(0, tree.getChildren()[2].getSymbol());
			tree = tree.getChildren()[0];
		}
		components.add(0, tree.getChildren()[0].getSymbol());
	}
	
	private void extractType(ParseTree tree) {
		assert(tree.getSymbol().equals("Type"));
		
		ParseTree firstChild = tree.getChildren()[0];
		if (firstChild.getSymbol().equals("PrimitiveType")) {
			extractPrimitiveType(firstChild);
		} else if (firstChild.getSymbol().equals("ReferenceType")) {
			extractReferenceType(firstChild);
		}
	}
	
	private void extractPrimitiveType(ParseTree tree) {
		assert(tree.getSymbol().equals("PrimitiveType"));
		
		ParseTree firstChild = tree.getChildren()[0];
		if (firstChild.getSymbol().equals("NumericType")) {
			tree = firstChild.getChildren()[0];
		}
		components.add(0, tree.getSymbol());
	}
	
	private void extractReferenceType(ParseTree tree) {
		assert(tree.getSymbol().equals("ReferenceType"));
		
		ParseTree firstChild = tree.getChildren()[0];
		if (firstChild.getSymbol().equals("ReferenceTypeArray")) {
			extractArrayType(firstChild.getChildren()[0]);
		} else if (firstChild.getSymbol().equals("ReferenceTypeNonArray")) {
			extractAmbiguousOrPackageName(firstChild.getChildren()[0]);
		}
	}
	
	private void extractArrayType(ParseTree tree) {
		assert(tree.getSymbol().equals("ArrayType"));
		
		ParseTree firstChild = tree.getChildren()[0];
		if (firstChild.getSymbol().equals("PrimitiveType")) {
			extractPrimitiveType(firstChild);
		} else if (firstChild.getSymbol().equals("ReferenceTypeNonArray")) {
			extractAmbiguousOrPackageName(firstChild);
		}
		components.add("[]");
	}
}
