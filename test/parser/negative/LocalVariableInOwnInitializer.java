public class LocalVariableInOwnInitializer {
  public LocalVariableInOwnInitializer() {}
  public void m() {
    int x  = (x = 1) + x;
  }
}
