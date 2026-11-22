package demo;

public class HtmlRenderer {
    // INTENTIONAL FLAW: untrusted input is embedded into HTML without escaping.
    public String welcome(String displayName) {
        return "<h1>Welcome, " + displayName + "</h1>";
    }
}
