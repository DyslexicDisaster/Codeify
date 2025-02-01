package codeify.persistance;

import codeify.business.Question;
import codeify.business.ProgrammingLanguage;

import java.sql.SQLException;
import java.util.List;

/**
 * Interface for Question database operations
 */
public interface QuestionDao {
    /**
     * Get all questions for a specific programming language
     *
     * @param languageId the ID of the programming language
     * @return List of questions for the specified language
     * @throws SQLException if database error occurs
     */
    List<Question> getQuestionsByLanguage(int languageId) throws SQLException;

    /**
     * Get all available programming languages
     *
     * @return List of all programming languages
     * @throws SQLException if database error occurs
     */
    List<ProgrammingLanguage> getAllProgrammingLanguages() throws SQLException;
}