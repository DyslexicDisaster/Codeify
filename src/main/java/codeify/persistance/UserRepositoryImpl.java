package codeify.persistance;

import codeify.entities.User;
import codeify.entities.role;
import codeify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;import java.sql.*;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    @Autowired
    private DataSource dataSource;

    @Override
    public Optional<User> findByUsername(String username) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setRegistrationDate(rs.getDate("registration_date").toLocalDate());
                user.setRole(role.valueOf(rs.getString("role")));
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }


    @Override
    public User save(User user) throws SQLException {
        String query = "INSERT INTO users (username, email, password, registration_date, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            statement.setDate(4, Date.valueOf(user.getRegistrationDate()));
            statement.setString(5, user.getRole().toString());

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    user.setUserId(generatedKeys.getInt(1));
                }
                return user;
            }
        }
        throw new SQLException("Failed to save user");
    }

}
