// "Suppress for method in injection" "true"
import org.intellij.lang.annotations.Language;

class X {
  @Language("JAVA")
  String s = """
    class EmptyArray {
      @SuppressWarnings("SillyAssignment")
      public void run() {
        int i = 0;
        i = i;
      }
    }
    """;
}
