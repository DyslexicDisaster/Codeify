package codeify.persistance;

import codeify.business.User;
import codeify.business.role;
import codeify.config.passwordHash;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.time.LocalDate;

public class UserDaoImpl extends MySQLDao implements UserDao {

    public UserDaoImpl(String dbName) { super(dbName);}

    /**
     * Registers a new user with the given username, password, and email.
     *
     * @param user the User object to register.
     * @return true if the user is successfully registered, false otherwise.
     * @throws SQLException if a database access error occurs.
     * @throws NoSuchAlgorithmException if the hashing algorithm is not available.
     * @throws InvalidKeySpecException if the key specification is not valid.
     */
    @Override
    public boolean register(User user) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            if (conn == null) {
                throw new SQLException("Unable to connect to the database!");
            }

            String checkUsernameQuery = "SELECT COUNT(*) FROM Users WHERE username = ?";
            stmt = conn.prepareStatement(checkUsernameQuery);
            stmt.setString(1, user.getUsername());
            ResultSet rs = stmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                System.out.println("Username already exists. Please choose a different username.");
                return false;
            }

            String checkEmailQuery = "SELECT COUNT(*) FROM Users WHERE email = ?";
            stmt = conn.prepareStatement(checkEmailQuery);
            stmt.setString(1, user.getEmail());
            rs = stmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                System.out.println("Email already exists. Please use a different email.");
                return false;
            }

            String salt = passwordHash.generateSalt();
            String hashedPassword = passwordHash.hashPassword(user.getPassword(), salt);

            String query = "INSERT INTO Users (username, email, password, salt, registration_date) VALUES (?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, hashedPassword);
            stmt.setString(4, salt);
            stmt.setDate(5, Date.valueOf(LocalDate.now()));

            return stmt.executeUpdate() > 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("A user with this email or username already exists.");
            return false;
        } finally {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    }

    /**
     * Logs in a user with the given username and password.
     *
     * @param username the username of the user to log in.
     * @param password the password of the user to log in.
     * @return a User object if the login is successful, null otherwise.
     * @throws SQLException if a database access error occurs.
     */
    @Override
    public User login(String username, String password) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            if (conn == null) {
                throw new SQLException("Unable to connect to the database!");
            }

            String query = "SELECT * FROM Users WHERE username = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String salt = rs.getString("salt");
                String hashedPassword = rs.getString("password");
                role userRole = role.valueOf(rs.getString("role"));
                LocalDate regDate = rs.getDate("registration_date").toLocalDate();

                if (passwordHash.validatePassword(password, hashedPassword, salt)) {
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            hashedPassword,
                            salt,
                            rs.getString("email"),
                            regDate,
                            userRole
                    );
                }
            }
        } finally {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }

        return null;
    }

    /**
     * Updates the user's information in the database.
     *
     * @param username name of the User objects to update.
     * @return true if the user is successfully updated, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean deleteUserByUsername(String username) throws SQLException {
        String query = "DELETE FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            return stmt.executeUpdate() > 0;
        }
    }
}
