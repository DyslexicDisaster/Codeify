package codeify.persistance;

import codeify.model.User;
import codeify.model.role;
import codeify.util.passwordHash;
import codeify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.time.LocalDate;

@Repository
public class UserRepositoryImpl implements UserRepository {

    @Autowired
    private DataSource dataSource;

    public boolean existsByUsername(String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        }
        return false;
    }

    public boolean existsByEmail(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        }
        return false;
    }

    @Override
    public boolean register(User user) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        String query = "INSERT INTO Users (username, email, password, salt, registration_date, role) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getSalt());
            statement.setDate(5, Date.valueOf(user.getRegistrationDate()));
            statement.setString(6, user.getRole().toString());

            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;
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
        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = null;

        try {
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
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            return stmt.executeUpdate() > 0;
        }
    }
}
