package codeify.service.implementations;

import codeify.entities.LastAttempt;
import codeify.entities.Question;
import codeify.entities.User;
import codeify.persistance.implementations.LastAttemptRepositoryImpl;
import codeify.persistance.implementations.QuestionRepositoryImpl;
import codeify.persistance.implementations.UserRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Optional;

@Service
public class LastAttemptService {

    @Autowired
    private LastAttemptRepositoryImpl lastAttemptRepository;

    @Autowired
    private UserRepositoryImpl userRepository;

    @Autowired
    private QuestionRepositoryImpl questionRepository;

    /**
     * Saves a user's last attempt at a question
     *
     * @param userId The ID of the user
     * @param questionId The ID of the question
     * @param code The code submitted by the user
     * @return true if saved successfully, false otherwise
     */
    public boolean saveLastAttempt(int userId, int questionId, String code) {
        try {
            Optional<User> userOpt = userRepository.getUserById(userId);
            Question question = questionRepository.getQuestionById(questionId);

            if (userOpt.isPresent() && question != null) {
                LastAttempt lastAttempt = new LastAttempt();
                lastAttempt.setUser(userOpt.get());
                lastAttempt.setQuestion(question);
                lastAttempt.setCode(code);

                return lastAttemptRepository.saveLastAttempt(lastAttempt);
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error saving last attempt: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves a user's last attempt at a question
     *
     * @param userId The ID of the user
     * @param questionId The ID of the question
     * @return code if found, or empty if not found
     */
    public Optional<String> getLastAttempt(int userId, int questionId) {
        try {
            Optional<LastAttempt> lastAttempt = lastAttemptRepository.findByUserIdAndQuestionId(userId, questionId);
            return lastAttempt.map(LastAttempt::getCode);
        } catch (SQLException e) {
            System.err.println("Error retrieving last attempt: " + e.getMessage());
            return Optional.empty();
        }
    }
}