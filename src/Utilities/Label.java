package Utilities;

import java.util.ArrayList;
import java.util.List;

import AbstractSyntax.Formal;

public class Label {
	
	private static int counter = 0;
	
	public static String generateLabel(String prefix) {
		return prefix + "_" + (++counter);
	}
	
	public static List<String> typesOfFormals(List<Formal> params) {
		List<String> strParams = new ArrayList<String>();
		for (Formal param : params) {
			strParams.add(param.getType().getCanonicalName());
		}
		return strParams;
	}
	
	public static String generateLabel(String prefix, String type, String name, List<String> params) {
		String toReturn = prefix + "_" + type;
		if (name != null) {
			toReturn += "#" + name;
		}
		if (params != null) {
			for (String param : params) {
				// Nasm doesn;t like [ or ] in comments.
				toReturn += "#" + param.replaceAll("\\[\\]", "@");
			}
		}
		return toReturn;
	}

}
