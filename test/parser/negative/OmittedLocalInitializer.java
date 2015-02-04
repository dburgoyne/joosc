public class OmittedLocalInitializer {
  public OmittedLocalInitializer() {}
  public void m() {
    int x /* = 0 */;
  }
}
