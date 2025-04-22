package codeify.persistance.implementations;

import codeify.entities.LastAttempt;
import codeify.entities.Question;
import codeify.entities.User;
import codeify.persistance.interfaces.LastAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class LastAttemptRepositoryImpl implements LastAttemptRepository {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserRepositoryImpl userRepository;

    @Autowired
    private QuestionRepositoryImpl questionRepository;

    /**
     * Finds the last attempt of a user for a specific question.
     *
     * @param userId the ID of the user
     * @param questionId the ID of the question
     * @return an Optional containing the LastAttempt if found, otherwise empty
     * @throws SQLException if a database access error occurs
     */
    @Override
    public Optional<LastAttempt> findByUserIdAndQuestionId(int userId, int questionId) throws SQLException {
        String query = "SELECT * FROM last_attempts WHERE user_id = ? AND question_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);
            statement.setInt(2, questionId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    LastAttempt lastAttempt = new LastAttempt();
                    lastAttempt.setId(resultSet.getInt("id"));

                    Optional<User> userOpt = userRepository.getUserById(resultSet.getInt("user_id"));
                    userOpt.ifPresent(lastAttempt::setUser);

                    Question question = questionRepository.getQuestionById(resultSet.getInt("question_id"));
                    lastAttempt.setQuestion(question);

                    lastAttempt.setCode(resultSet.getString("code"));

                    Timestamp createdAt = resultSet.getTimestamp("created_at");
                    if (createdAt != null) {
                        lastAttempt.setCreatedAt(createdAt.toLocalDateTime());
                    }

                    Timestamp updatedAt = resultSet.getTimestamp("updated_at");
                    if (updatedAt != null) {
                        lastAttempt.setUpdatedAt(updatedAt.toLocalDateTime());
                    }

                    return Optional.of(lastAttempt);
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Saves the last attempt of a user for a specific question.
     * If an attempt already exists, it updates the existing record.
     *
     * @param lastAttempt the LastAttempt object to save
     * @return true if the operation was successful, false otherwise
     * @throws SQLException if a database access error occurs
     */
    @Override
    public boolean saveLastAttempt(LastAttempt lastAttempt) throws SQLException {
        Optional<LastAttempt> existingAttempt = findByUserIdAndQuestionId(
                lastAttempt.getUser().getUserId(),
                lastAttempt.getQuestion().getId()
        );

        if (existingAttempt.isPresent()) {
            String updateQuery = "UPDATE last_attempts SET code = ?, updated_at = ? WHERE id = ?";

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(updateQuery)) {

                statement.setString(1, lastAttempt.getCode());
                statement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                statement.setInt(3, existingAttempt.get().getId());

                return statement.executeUpdate() > 0;
            }
        } else {
            String insertQuery = "INSERT INTO last_attempts (user_id, question_id, code, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(insertQuery)) {

                statement.setInt(1, lastAttempt.getUser().getUserId());
                statement.setInt(2, lastAttempt.getQuestion().getId());
                statement.setString(3, lastAttempt.getCode());

                LocalDateTime now = LocalDateTime.now();
                statement.setTimestamp(4, Timestamp.valueOf(now));
                statement.setTimestamp(5, Timestamp.valueOf(now));

                return statement.executeUpdate() > 0;
            }
        }
    }
}