public class ImpicitThisClassForStaticMethods {
    public ImpicitThisClassForStaticMethods(){}
  public static int m1() {
    return 42;
  }
  public int m2() {
    return m1();
  }
}
