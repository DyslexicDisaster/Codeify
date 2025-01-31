package codeify.persistance;

import codeify.business.User;
import codeify.config.passwordHash;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.time.LocalDate;

public class UserDaoImpl extends MySQLDao implements UserDao {

    public UserDaoImpl(String dbName) { super(dbName);}


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

            // catching exceptions when user tries to insert a duplicate or trying to insert a
            // foreign key value that doesn't exist or violating a check constraint on a column
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("A user with this email or username already exists.");
            return false;
        } finally {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    }
}
