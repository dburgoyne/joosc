public class ImplicitThisForMethods {
  public int m1() {
    return 42;
  }
  public int m2() {
    return m1();
  }
  public ImplicitThisForMethods() {}
}
