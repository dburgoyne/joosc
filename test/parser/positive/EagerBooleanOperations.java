public class EagerBooleanOperations {
  public EagerBooleanOperations() {}
  public boolean m(boolean x) {
    return (x & true) | !x;
  }
}
