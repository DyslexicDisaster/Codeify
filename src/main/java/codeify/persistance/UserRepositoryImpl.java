package codeify.persistance;

import codeify.entities.User;
import codeify.entities.role;
import codeify.util.JwtUtil;
import codeify.util.passwordHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    @Autowired
    private DataSource dataSource;

    /**
     * Registers a new user with the given details.
     */
    @Override
    public boolean register(User user) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        String query = "INSERT INTO users (username, email, password, registration_date, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Generate salt and hash password
            String salt = passwordHash.generateSalt();
            String hashedPassword = passwordHash.hashPassword(user.getPassword(), salt);
            String saltedHashedPassword = salt + ":" + hashedPassword;

            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, saltedHashedPassword);
            statement.setDate(4, Date.valueOf(user.getRegistrationDate()));
            statement.setString(5, user.getRole().toString());

            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;
        }
    }

    /**
     * Logs in a user by verifying the username and password.
     */
    @Override
    public String login(String username, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");

                    // Split the stored password to get salt and hash
                    String[] parts = storedPassword.split(":");
                    if (parts.length != 2) {
                        return null;
                    }
                    String salt = parts[0];
                    String storedHash = parts[1];

                    // Hash the incoming password with the stored salt
                    String hashedInputPassword = passwordHash.hashPassword(password, salt);

                    if (hashedInputPassword.equals(storedHash)) {
                        return JwtUtil.generateToken(username);
                    }
                }
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeySpecException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

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
                            role.valueOf(resultSet.getString("role"))
                    );
                    return Optional.of(user); // Wrap the user object in Optional
                }
            }
        }
        return Optional.empty(); // Return an empty Optional if not found
    }

    /**
     * Check if a username already exists.
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
     * OAuth2 login and automatic registration.
     */
    public User oauth2Login(String email, String name) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        String query = "SELECT * FROM users WHERE email = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email"),
                            rs.getDate("registration_date").toLocalDate(),
                            role.valueOf(rs.getString("role"))
                    );
                } else {
                    User newUser = new User(name, "", email, LocalDate.now());
                    register(newUser);
                    return newUser;
                }
            }
        }
    }

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
                        role.valueOf(resultSet.getString("role"))
                );
                users.add(user);
            }
        }
        return users;
    }

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
                            role.valueOf(resultSet.getString("role"))
                    );
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean deleteUserById(int userId) throws SQLException {
        String query = "DELETE FROM users WHERE user_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            return statement.executeUpdate() > 0;
        }
    }

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