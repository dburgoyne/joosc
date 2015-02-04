public class LazyBooleanOperations {
  public LazyBooleanOperations() {}
  public boolean m(boolean x) {
    return (x && true) || x;
  }
}
