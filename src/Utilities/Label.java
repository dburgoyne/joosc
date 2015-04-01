package Utilities;

import java.util.List;
import java.util.UUID;

public class Label {
	
	public static String generateLabel(String prefix) {
		return prefix + "_" + UUID.randomUUID().toString();
	}
	
	public static String generateLabel(String prefix, String type, String name, List<String> params) {
		String toReturn = prefix + "_" + type;
		if (name != null) {
			toReturn += "#" + name;
		}
		if (params != null) {
			for (String param : params) {
				toReturn += "#" + param;
			}
		}
		return toReturn;
	}

}
