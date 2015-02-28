package AbstractSyntax;

import java.util.ArrayList;
import java.util.List;

import Parser.ParseTree;

public enum Modifier {
	PUBLIC,
	PROTECTED,
	ABSTRACT,
	STATIC,
	FINAL,
	NATIVE;
	
	public static Modifier fromString(String s) {
		return Modifier.valueOf(s.toUpperCase());
	}
	
	// Extracts modifiers from a Modifiers node.
	public static List<Modifier> extractModifiers(ParseTree tree) {
		List<Modifier> modifiers = new ArrayList<Modifier>();
		while (tree.getChildren().length > 1) {
			Modifier modifier = Modifier.fromString(tree.getChildren()[1].getSymbol());
			modifiers.add(modifier);
			tree = tree.getChildren()[0];
		}
		Modifier modifier = Modifier.fromString(tree.getChildren()[0].getSymbol());
		modifiers.add(modifier);
		return modifiers;
	}
}
