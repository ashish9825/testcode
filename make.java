import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private static Connection connection;

    public static void main(String[] args) {
        UserService service = new UserService();

        // Test 1 — get a user
        User user = service.getUserById(1);
        System.out.println("User email: " + user.email);

        // Test 2 — login
        boolean loggedIn = service.login("admin", "password123");
        System.out.println("Logged in: " + loggedIn);

        // Test 3 — get all users
        List<User> users = service.getAllUsers();
        System.out.println("Total users: " + users.size());

        // Test 4 — divide
        System.out.println("Result: " + service.divide(10, 0));
    }

    // ── BUG 1: Null reference ─────────────────────────────────────────────────
    // getUserById can return null if user not found.
    // Calling user.email on line 17 will throw NullPointerException.
    public User getUserById(int id) {
        if (id <= 0) {
            return null;
        }
        return new User(id, "ashish@example.com", "Ashish");
    }

    // ── BUG 2: SQL Injection ──────────────────────────────────────────────────
    // User input is directly concatenated into the SQL query.
    // An attacker can pass username = "' OR '1'='1" to bypass authentication.
    public boolean login(String username, String password) {
        try {
            String query = "SELECT * FROM users WHERE username='"
                    + username + "' AND password='" + password + "'";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }

    // ── BUG 3: Resource leak ──────────────────────────────────────────────────
    // Connection and Statement are opened but never closed.
    // This will exhaust the DB connection pool over time.
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try {
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/mydb", "root", "password"
            );
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM users");
            while (rs.next()) {
                users.add(new User(
                    rs.getInt("id"),
                    rs.getString("email"),
                    rs.getString("name")
                ));
            }
            // conn and stmt are never closed — resource leak
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return users;
    }

    // ── BUG 4: Division by zero ───────────────────────────────────────────────
    // No check for divisor = 0. Throws ArithmeticException at runtime.
    public int divide(int a, int b) {
        return a / b;
    }

    // ── BUG 5: Off-by-one error ───────────────────────────────────────────────
    // Loop goes from 0 to list.size() INCLUSIVE — throws IndexOutOfBoundsException
    // on the last iteration because valid indexes are 0 to size()-1.
    public void printAllUsers(List<User> users) {
        for (int i = 0; i <= users.size(); i++) {
            System.out.println(users.get(i).name);
        }
    }

    // ── BUG 6: Credentials hardcoded ─────────────────────────────────────────
    // Database password is hardcoded in source code.
    // Anyone with access to this file has the DB password.
    public Connection getConnection() throws Exception {
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/mydb",
            "root",
            "SuperSecret123!"
        );
    }

    // Simple User model
    static class User {
        int id;
        String email;
        String name;

        User(int id, String email, String name) {
            this.id    = id;
            this.email = email;
            this.name  = name;
        }
    }
}