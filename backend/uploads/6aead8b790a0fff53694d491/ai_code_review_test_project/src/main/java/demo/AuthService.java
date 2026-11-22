package demo;

public class AuthService {
    // INTENTIONAL FLAW: fake hardcoded credential for scanner testing only.
    private static final String ADMIN_PASSWORD = "demo-password-123";

    // INTENTIONAL FLAW: compares raw passwords instead of using a password hash.
    public boolean login(String suppliedPassword) {
        return ADMIN_PASSWORD.equals(suppliedPassword);
    }
}
