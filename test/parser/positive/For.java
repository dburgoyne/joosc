public class For {
  public For() {}
  public int m(int x) {
    int y = 0;
    for (int i=x; i>0; i=i-1) y=y+1;
    return y;
  }
}
