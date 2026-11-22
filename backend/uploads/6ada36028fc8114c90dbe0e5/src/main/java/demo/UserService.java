package demo;

public class UserService {
    // Intentional issue: null input can cause NullPointerException.
    public String displayName(String name) {
        return "Hello, " + name.trim();
    }

    // Intentional issue: null role can cause NullPointerException.
    public boolean isAdmin(String role) {
        return role.equals("ADMIN");
    }
}
