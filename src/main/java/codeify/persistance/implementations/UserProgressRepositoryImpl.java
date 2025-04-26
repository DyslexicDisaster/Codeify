package codeify.persistance.implementations;

import codeify.entities.Question;
import codeify.entities.UserProgress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                        String status = grade >= 70 ? "COMPLETED" : "IN_PROGRESS";
                        String updateQuery = "UPDATE user_progress SET score = ?, status = ?, last_attempt = CURRENT_TIMESTAMP WHERE id = ?";
                        try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                            updateStmt.setInt(1, grade);
                            updateStmt.setString(2, status);
                            updateStmt.setInt(3, id);
                            int rowsAffected = updateStmt.executeUpdate();
                            return rowsAffected > 0;
                        }
                    } else {
                        String status = grade >= 70 ? "COMPLETED" : "IN_PROGRESS";
                        String insertQuery = "INSERT INTO user_progress (user_id, question_id, score, status, last_attempt) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
                        try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                            insertStmt.setInt(1, userId);
                            insertStmt.setInt(2, questionId);
                            insertStmt.setInt(3, grade);
                            insertStmt.setString(4, status);
                            int rowsInserted = insertStmt.executeUpdate();
                            return rowsInserted > 0;
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets the user's progress for a specific programming language.
     * Calculates completion statistics and returns detailed progress information.
     *
     * @param userId     The ID of the user.
     * @param languageId The ID of the programming language.
     * @return A map containing progress data: progressPercentage, completedQuestions, totalQuestions, and progressDetails.
     * @throws SQLException if a database error occurs.
     */
    public Map<String, Object> getUserProgressForLanguage(int userId, int languageId) throws SQLException {
        Map<String, Object> progressData = new HashMap<>();
        List<Map<String, Object>> progressDetails = new ArrayList<>();
        int completedQuestions = 0;
        int totalQuestions = 0;

        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT q.id, q.title, q.difficulty, q.programming_language_id, " +
                    "up.status, up.score, up.last_attempt " +
                    "FROM questions q " +
                    "LEFT JOIN user_progress up ON q.id = up.question_id AND up.user_id = ? " +
                    "WHERE q.programming_language_id = ? " +
                    "ORDER BY q.difficulty, q.id";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, userId);
                statement.setInt(2, languageId);

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        totalQuestions++;

                        Map<String, Object> questionProgress = new HashMap<>();
                        Map<String, Object> questionData = new HashMap<>();
                        questionData.put("id", rs.getInt("id"));
                        questionData.put("title", rs.getString("title"));
                        questionData.put("difficulty", rs.getString("difficulty"));

                        questionProgress.put("question", questionData);

                        String status = rs.getString("status");
                        //f no progress record exists
                        if (status == null) {
                            status = "NOT_STARTED";
                        }

                        questionProgress.put("status", status);
                        questionProgress.put("score", rs.getObject("score") != null ? rs.getInt("score") : 0);

                        if (rs.getTimestamp("last_attempt") != null) {
                            questionProgress.put("lastAttempt", rs.getTimestamp("last_attempt").toLocalDateTime());
                        }

                        if ("COMPLETED".equals(status)) {
                            completedQuestions++;
                        }

                        progressDetails.add(questionProgress);
                    }
                }
            }

            double progressPercentage = totalQuestions > 0 ? (double) completedQuestions / totalQuestions * 100 : 0;

            progressPercentage = Math.round(progressPercentage * 100.0) / 100.0;

            progressData.put("progressPercentage", progressPercentage);
            progressData.put("completedQuestions", completedQuestions);
            progressData.put("totalQuestions", totalQuestions);
            progressData.put("progressDetails", progressDetails);

            return progressData;
        }
    }

    public int getTotalScore(int userId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(score),0) FROM user_progress WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Gets the total number of question attempts across all users
     *
     * @return The total number of attempts
     * @throws SQLException if a database error occurs
     */
    public int getTotalAttempts() throws SQLException {
        String sql = "SELECT COUNT(*) FROM user_progress";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Gets the total number questions completed by all users
     *
     * @return The total number of completed questions
     * @throws SQLException if a database error occurs
     */
    public int getTotalCompletedQuestions() throws SQLException {
        String sql = "SELECT COUNT(*) FROM user_progress WHERE status = 'COMPLETED'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Gets the number of attempts for a specific day of the week monday = 1 and sunday =7
     *
     * @param dayOfWeek The day of the week from 1 to 7
     * @return The number of attempts on that day
     * @throws SQLException if a database error occurs
     */
    public int getAttemptsForDayOfWeek(int dayOfWeek) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user_progress WHERE DAYOFWEEK(last_attempt) = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dayOfWeek);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Gets the number of questions completed on a specific day
     *
     * @param dayOfWeek The day of week 1-7
     * @return The number of completions on that day
     * @throws SQLException if a database error occurs
     */
    public int getCompletionsForDayOfWeek(int dayOfWeek) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user_progress WHERE DAYOFWEEK(last_attempt) = ? AND status = 'COMPLETED'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dayOfWeek);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Gets the number of attempts for a specific question
     *
     * @param questionId The ID of the question
     * @return The number of attempts for that question
     * @throws SQLException if a database error occurs
     */
    public int getAttemptsForQuestion(int questionId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user_progress WHERE question_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Gets the rate of completion for questions
     *
     * @param questionId The ID of the question
     * @return The completion rate as a percentage
     * @throws SQLException if a database error occurs
     */
    public int getCompletionRateForQuestion(int questionId) throws SQLException {
        String sql = "SELECT " +
                "COUNT(*) AS attempts, " +
                "SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) AS completions " +
                "FROM user_progress WHERE question_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int attempts = rs.getInt("attempts");
                    int completions = rs.getInt("completions");
                    return attempts > 0 ? (completions * 100 / attempts) : 0;
                }
                return 0;
            }
        }
    }

    /**
     * Gets the average score for questions of a specific programming language
     *
     * @param languageId The ID of the programming language
     * @return The average score
     * @throws SQLException if a database error occurs
     */
    public int getAverageScoreForLanguage(int languageId) throws SQLException {
        String sql = "SELECT AVG(up.score) AS avg_score " +
                "FROM user_progress up " +
                "JOIN questions q ON up.question_id = q.id " +
                "WHERE q.programming_language_id = ? AND up.status = 'COMPLETED'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, languageId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? (int)Math.round(rs.getDouble("avg_score")) : 0;
            }
        }
    }

    /**
     * Gets the average score for questions of a specific difficulty
     *
     * @param difficulty The difficulty level
     * @return The average score
     * @throws SQLException if a database error occurs
     */
    public int getAverageScoreForDifficulty(String difficulty) throws SQLException {
        String sql = "SELECT AVG(up.score) AS avg_score " +
                "FROM user_progress up " +
                "JOIN questions q ON up.question_id = q.id " +
                "WHERE q.difficulty = ? AND up.status = 'COMPLETED'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, difficulty);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? (int)Math.round(rs.getDouble("avg_score")) : 0;
            }
        }
    }
}