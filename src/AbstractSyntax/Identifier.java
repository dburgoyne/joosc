package AbstractSyntax;

import java.util.ArrayList;
import java.util.List;

import Parser.ParseTree;

// TODO Should override equals(), hashCode()
public class Identifier extends Expression {

	protected List<String> components;
	
	// Convenience functions.
	protected boolean isArray() {
		return components.get(components.size() - 1).equals("[]");
	}
	
	protected boolean isStarImport() {
		return components.get(components.size() - 1).equals("*");
	}
	
	// Flattens almost anything.
	public Identifier(ParseTree tree) {
		super(tree);
		assert(tree.getSymbol().equals("AmbiguousName")
			|| tree.getSymbol().equals("PackageName")
			|| tree.getSymbol().equals("Type")
			|| tree.isTerminal());
		components = new ArrayList<String>();
		
		// Type has a different grammatical structure than the others.
		if (tree.getSymbol().equals("Type")) {
			extractType(tree);
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
			ParseTree ggChild = firstChild.getChildren()[0].getChildren()[0];
			if (ggChild.getSymbol().equals("PrimitiveType")) {
				extractPrimitiveType(ggChild);
			} else if (ggChild.getSymbol().equals("ReferenceTypeNonArray")) {
				extractAmbiguousOrPackageName(ggChild);
			}
			components.add("[]");
		} else if (firstChild.getSymbol().equals("ReferenceTypeNonArray")) {
			extractAmbiguousOrPackageName(firstChild.getChildren()[0]);
		}
	}
}
