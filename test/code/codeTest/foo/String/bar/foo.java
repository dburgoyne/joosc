package foo.String.bar;

public class foo {
	public static int mo = 10 + 10;
	
	public int x = 1;
	public int xx = 1 + 1;
	public int xxx = 1 + xx;
	
	public foo() {
	}
	
	public foo(int y) {
		xx = y;
	}
	
	public int xfoo() {
		return xfoo(xx);
	}
	
	public int xfoo(int z) {
		int y = 1;
		x = y;
		x = z + x;
		return x;
	}
}
