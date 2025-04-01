package codeify.persistance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;

@Repository
public class UserProgressRepositoryImpl {

    @Autowired
    private DataSource dataSource;

    /**
     * Updates the user progress by setting the score, marking the status as COMPLETED,
     * and updating the last_attempt timestamp.
     * If no record exists for the given user and question, a new record is inserted.
     *
     * @param userId     The ID of the user.
     * @param questionId The ID of the question.
     * @param grade      The score/grade to record.
     * @return true if the update or insert was successful; false otherwise.
     * @throws SQLException if a database error occurs.
     */
    public boolean updateUserProgress(int userId, int questionId, int grade) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            // Check if a record exists for the given user_id and question_id.
            String checkQuery = "SELECT id FROM user_progress WHERE user_id = ? AND question_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(checkQuery)) {
                statement.setInt(1, userId);
                statement.setInt(2, questionId);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        // Record exists, update it.
                        int id = rs.getInt("id");
                        String updateQuery = "UPDATE user_progress SET score = ?, status = 'COMPLETED', last_attempt = CURRENT_TIMESTAMP WHERE id = ?";
                        try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                            updateStmt.setInt(1, grade);
                            updateStmt.setInt(2, id);
                            int rowsAffected = updateStmt.executeUpdate();
                            return rowsAffected > 0;
                        }
                    } else {
                        // Record does not exist, insert a new record.
                        String insertQuery = "INSERT INTO user_progress (user_id, question_id, score, status, last_attempt) VALUES (?, ?, ?, 'COMPLETED', CURRENT_TIMESTAMP)";
                        try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                            insertStmt.setInt(1, userId);
                            insertStmt.setInt(2, questionId);
                            insertStmt.setInt(3, grade);
                            int rowsInserted = insertStmt.executeUpdate();
                            return rowsInserted > 0;
                        }
                    }
                }
            }
        }
    }
}
