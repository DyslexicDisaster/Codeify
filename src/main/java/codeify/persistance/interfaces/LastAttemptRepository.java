package codeify.persistance.interfaces;

import codeify.entities.LastAttempt;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Optional;

@Repository
public interface LastAttemptRepository {
    Optional<LastAttempt> findByUserIdAndQuestionId(int userId, int questionId) throws SQLException;
    boolean saveLastAttempt(LastAttempt lastAttempt) throws SQLException;
}