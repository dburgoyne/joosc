package Utilities;

public class ObjectUtils {
	public static boolean equals(Object object1, Object object2) {
	    return (object1 == null || object2 == null) ?  object1 == object2 : object1.equals(object2);
	}
}
