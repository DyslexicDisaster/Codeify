package codeify.persistance.implementations;

import codeify.entities.User;
import codeify.entities.role;
import codeify.persistance.interfaces.UserRepository;
import codeify.security.JwtUtil;
import codeify.util.passwordHash;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class UserRepositoryImpl implements UserRepository {

    @Autowired
    private DataSource dataSource;

    //TESTED
    /*
     * Updates the password of a user in the database.
     *
     * @param id  ID of the user
     * @param pwd New password for the user
     */
    @Override
    public void updatePassword(int id, String pwd) {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pwd);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Error updating password for user ID {}: {}", id, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    //TESTED
    /*
     * Saves a new user to the database.
     *
     * @param user User object to be saved
     * @return Saved user object with generated ID
     * @throws SQLException If a database access error occurs
     */
    @Override
    public User save(User user) throws SQLException {
        String sql = """
            INSERT INTO users (
                username, password, email,
                registration_date, role, provider
            ) VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setDate(4, Date.valueOf(user.getRegistrationDate()));
            ps.setString(5, user.getRole().name());
            ps.setString(6, user.getProvider());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setUserId(keys.getInt(1));
                }
            }
            return user;
        }
    }

    /**
     * Registers a new user with the given details.
     */
    @Override
    public boolean register(User user) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        String query = "INSERT INTO users (username, password, email, registration_date, role, provider) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            String passwordValue = null;
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                String salt = passwordHash.generateSalt();
                String hashedPassword = passwordHash.hashPassword(user.getPassword(), salt);
                passwordValue = salt + ":" + hashedPassword;
            }

            statement.setString(1, user.getUsername());
            statement.setString(2, passwordValue);
            statement.setString(3, user.getEmail());
            statement.setDate(4, java.sql.Date.valueOf(user.getRegistrationDate()));
            statement.setString(5, user.getRole().toString());
            statement.setString(6, user.getProvider());

            return statement.executeUpdate() > 0;
        }
    }


    /**
     * Logs in a user with the given username and password.
     *
     * @param username Username of the user
     * @param password Password of the user
     * @return JWT token if login is successful, null otherwise
     * @throws SQLException If a database access error occurs
     */
    @Override
    public String login(String username, String password) throws SQLException {
        Optional<User> userOpt = findByUsername(username);
        if (userOpt.isEmpty()) {
            log.warn("User not found: {}", username);
            return null;
        }

        User user = userOpt.get();

        if (user.getPassword() == null) {
            log.warn("OAuth user {} attempted password login", username);
            throw new BadCredentialsException("OAuth users must login via their provider");
        }

        String[] parts = user.getPassword().split(":");
        if (parts.length != 2) {
            log.warn("Invalid password format for user {}", username);
            return null;
        }

        try {
            String salt = parts[0];
            String storedHash = parts[1];
            String hashedInputPassword = passwordHash.hashPassword(password, salt);

            if (hashedInputPassword.equals(storedHash)) {
                return JwtUtil.generateToken(username);
            }
        } catch (Exception e) {
            log.error("Password verification failed", e);
        }

        return null;
    }

    /**
     * Finds a user by their email address.
     *
     * @param email Email address of the user
     * @return Optional containing the user if found, empty otherwise
     * @throws SQLException If a database access error occurs
     */
    @Override
    public Optional<User> findByEmail(String email) throws SQLException {
        String query = "SELECT * FROM users WHERE email = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = new User(
                            resultSet.getInt("user_id"),
                            resultSet.getString("username"),
                            resultSet.getString("password"),
                            resultSet.getString("email"),
                            resultSet.getDate("registration_date").toLocalDate(),
                            role.valueOf(resultSet.getString("role")),
                            resultSet.getString("provider")
                    );
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds a user by their username.
     *
     * @param username Username of the user
     * @return Optional containing the user if found, empty otherwise
     * @throws SQLException If a database access error occurs
     */
    @Override
    public Optional<User> findByUsername(String username) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = new User(
                            resultSet.getInt("user_id"),
                            resultSet.getString("username"),
                            resultSet.getString("password"),
                            resultSet.getString("email"),
                            resultSet.getDate("registration_date").toLocalDate(),
                            role.valueOf(resultSet.getString("role")),
                            resultSet.getString("provider")
                    );
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Check if a username already exists.
     *
     * @param username Username to check
     * @return true if the username exists, false otherwise
     * @throws SQLException If a database access error occurs
     */
    @Override
    public boolean existsByUsername(String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Check if an email already exists.
     *
     * @param email Email to check
     * @return true if the email exists, false otherwise
     * @throws SQLException If a database access error occurs
     */
    @Override
    public boolean existsByEmail(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Get all users from the database.
     *
     * @return List of all users
     * @throws SQLException If a database access error occurs
     */
    @Override
    public List<User> getAllUsers() throws SQLException {
        String query = "SELECT * FROM users";
        List<User> users = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                User user = new User(
                        resultSet.getInt("user_id"),
                        resultSet.getString("username"),
                        resultSet.getString("password"),
                        resultSet.getString("email"),
                        resultSet.getDate("registration_date").toLocalDate(),
                        role.valueOf(resultSet.getString("role")),
                        resultSet.getString("provider")
                );
                users.add(user);
            }
        }
        return users;
    }

    /**
     * Get a user by their ID.
     *
     * @param userId ID of the user
     * @return Optional containing the user if found, empty otherwise
     * @throws SQLException If a database access error occurs
     */
    @Override
    public Optional<User> getUserById(int userId) throws SQLException {
        String query = "SELECT * FROM users WHERE user_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = new User(
                            resultSet.getInt("user_id"),
                            resultSet.getString("username"),
                            resultSet.getString("password"),
                            resultSet.getString("email"),
                            resultSet.getDate("registration_date").toLocalDate(),
                            role.valueOf(resultSet.getString("role")),
                            resultSet.getString("provider")
                    );
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Delete a user by their ID.
     *
     * @param userId ID of the user
     * @return true if the user was deleted, false otherwise
     * @throws SQLException If a database access error occurs
     */
    @Override
    public boolean deleteUserById(int userId) throws SQLException {
        String query = "DELETE FROM users WHERE user_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Update a user's details.
     *
     * @param user User object with updated details
     * @return true if the user was updated, false otherwise
     * @throws SQLException If a database access error occurs
     */
    @Override
    public boolean updateUser(User user) throws SQLException {
        String query = "UPDATE users SET username = ?, email = ?, password = ?, role = ? WHERE user_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getRole().name());
            statement.setInt(5, user.getUserId());
            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Change a user's role.
     *
     * @param id ID of the user
     * @param role New role for the user
     * @return true if the role was changed, false otherwise
     * @throws SQLException If a database access error occurs
     */
    @Override
    public boolean changeRole(int id, role role) throws SQLException {
        String query = "UPDATE users SET role = ? WHERE user_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, role.name());
            statement.setInt(2, id);
            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Reset a user's password.
     *
     * @param id ID of the user
     * @param hashedPassword New hashed password
     * @return true if the password was reset, false otherwise
     * @throws SQLException If a database access error occurs
     */
    @Override
    public boolean resetPassword(int id, String hashedPassword) throws SQLException {
        String query = "UPDATE users SET password = ? WHERE user_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, hashedPassword);
            statement.setInt(2, id);
            return statement.executeUpdate() > 0;
        }
    }
}