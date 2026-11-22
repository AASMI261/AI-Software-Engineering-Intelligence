package demo;

public class UserRepository {
    // INTENTIONAL FLAW: user input is concatenated into SQL.
    public String findUserSql(String username) {
        return "SELECT * FROM users WHERE username = '" + username + "'";
    }
}
