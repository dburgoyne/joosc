public class NonThisFieldAccess {
  public NonThisFieldAccess() {}
  public int x;
  public void m() {
    new NonThisFieldAccess().x = 42;
  }
}
