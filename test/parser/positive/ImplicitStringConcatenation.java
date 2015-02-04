public class ImplicitStringConcatenation {
  public ImplicitStringConcatenation() {}
  public String m(int x) {
    return "foo" + x + true + null;
  }
}
