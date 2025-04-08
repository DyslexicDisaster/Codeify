package codeify.persistance.interfaces;

import codeify.entities.LastAttempt;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Optional;

@Repository
public interface LastAttemptRepository {
    /**
     * Find a user's last attempt for a specific question
     *
     * @param userId the ID of the user
     * @param questionId the ID of the question
     * @return LastAttempt if found
     * @throws SQLException if a database error occurs
     */
    Optional<LastAttempt> findByUserIdAndQuestionId(int userId, int questionId) throws SQLException;

    /**
     * Save a user's last attempt for a question
     *
     * @param lastAttempt the LastAttempt entity to save
     * @return true if saved successfully, false otherwise
     * @throws SQLException if a database error occurs
     */
    boolean saveLastAttempt(LastAttempt lastAttempt) throws SQLException;
}