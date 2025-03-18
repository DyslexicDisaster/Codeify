package codeify.persistance;

import codeify.entities.User;
import codeify.entities.role;
import codeify.util.JwtUtil;
import codeify.util.passwordHash;
import codeify.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private static final Logger log = LoggerFactory.getLogger(UserRepositoryImpl.class);

    @Autowired
    private DataSource dataSource;

    // Constructor
    public UserRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Checks if a user with the given username exists in the database.
     *
     * @param username the username to check.
     * @return true if the user exists, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    @Override
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

    /**
     * Checks if a user with the given email exists in the database.
     *
     * @param email the email to check.
     * @return true if the user exists, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    @Override
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

    /**
     * Changes the role of a user with the given id.
     *
     * @param id the id of the user to change the role.
     * @param role the new role to assign to the user.
     * @return true if the role is successfully changed, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    @Override
    public boolean changeRole(int id, role role) throws SQLException {
        String query = "UPDATE Users SET role = ? WHERE user_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, String.valueOf(role));
            statement.setInt(2, id);

            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        }
    }

    /**
     * Registers a new user in the database.
     *
     * @param user the User object to register.
     * @return true if the user is successfully registered, false otherwise.
     * @throws SQLException if a database access error occurs.
     * @throws NoSuchAlgorithmException if the algorithm used to hash the password is not found.
     * @throws InvalidKeySpecException if the key specification used to hash the password is invalid.
     */
    @Override
    public boolean register(User user) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        String query = "INSERT INTO Users (username, email, password, salt, registration_date, role) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            String salt = passwordHash.generateSalt();
            String hashedPassword = passwordHash.hashPassword(user.getPassword(), salt);
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, hashedPassword);
            statement.setString(4, salt);
            statement.setDate(5, Date.valueOf(LocalDate.now()));
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
    public String login(String username, String password) throws SQLException {
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

                if (passwordHash.validatePassword(password, hashedPassword, salt)) {
                    return JwtUtil.generateToken(username);
                }
            }
        } finally {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
        return null;
    }

    /**
     * Deletes a user with the given user_id.
     *
     * @param id the user_id of the user to delete.
     * @return true if the user is successfully deleted, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    @Override
    public boolean deleteUserById(int id) throws SQLException {
        String query = "DELETE FROM Users WHERE user_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            int rowsDeleted = statement.executeUpdate();
            return rowsDeleted > 0;
        }
    }

    /**
     * Get all users from the database.
     *
     * @return a list of all users in the database.
     * @throws SQLException if a database access error occurs.
     */
    @Override
    public List<User> getAllUsers() throws SQLException {
        String query = "SELECT * FROM Users";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();
            List<User> users = new ArrayList<>();
            while (resultSet.next()) {
                users.add(new User(
                        resultSet.getInt("user_id"),
                        resultSet.getString("username"),
                        resultSet.getString("password"),
                        resultSet.getString("salt"),
                        resultSet.getString("email"),
                        resultSet.getDate("registration_date").toLocalDate(),
                        role.valueOf(resultSet.getString("role"))
                ));
            }
            return users;
        }
    }

    /**
     * Updates the user with the given user_id.
     *
     * @param user the User object to update.
     * @return
     * @throws SQLException
     */
    @Override
    public boolean updateUser(User user) throws SQLException {
        String query = "UPDATE Users SET username = ?, email = ?, password = ?, role = ? WHERE user_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getRole().toString());
            statement.setInt(5, user.getUserId());

            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        }
    }

    /**
     * Get a user by user_id.
     *
     * @param userId the user_id of the user to get.
     * @return a User object if the user exists, null otherwise.
     * @throws SQLException if a database access error occurs.
     */
    @Override
    public User getUserById(int userId) throws SQLException {
        String query = "SELECT * FROM users WHERE user_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("User found: " + rs.getString("username"));
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("salt"),
                            rs.getString("email"),
                            rs.getDate("registration_date").toLocalDate(),
                            codeify.entities.role.valueOf(rs.getString("role"))
                    );
                } else {
                    System.out.println("No user found for user_id=" + userId);
                }
            }
        }
        return null;
    }

    /**
     * Reset the password of a user with the given user_id.
     *
     * @param id the user_id of the user to reset the password.
     * @param password the new password to set.
     * @return true if the password is successfully reset, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    @Override
    public boolean resetPassword(int id, String password) throws SQLException {
        String query = "UPDATE Users SET password = ? WHERE user_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, password);
            statement.setInt(2, id);

            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        }
    }
}
