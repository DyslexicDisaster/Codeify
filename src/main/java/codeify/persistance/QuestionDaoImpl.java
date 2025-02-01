package codeify.persistance;

import codeify.business.Question;
import codeify.business.ProgrammingLanguage;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class QuestionDaoImpl implements QuestionDao {
    private final MySQLDao mySQLDao;

    public QuestionDaoImpl(String propertiesPath) {
        this.mySQLDao = new MySQLDao(propertiesPath);
    }

    /**
     *
     * @param languageId gets all questions from database for programming language based on id
     * @return a list of questions
     * @throws SQLException if there is error in getting questions
     */
    @Override
    public List<Question> getQuestionsByLanguage(int languageId) throws SQLException {
        List<Question> questions = new ArrayList<>();

        String sql = """
            SELECT q.*, pl.name as language_name 
            FROM questions q
            JOIN programming_languages pl ON q.programming_language_id = pl.id
            WHERE pl.id = ?
            ORDER BY q.difficulty, q.id
            """;

        try (Connection conn = mySQLDao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, languageId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ProgrammingLanguage language = new ProgrammingLanguage(
                            rs.getInt("programming_language_id"),
                            rs.getString("language_name")
                    );

                    Question question = new Question(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            language,
                            Question.QuestionType.valueOf(rs.getString("question_type")),
                            Question.Difficulty.valueOf(rs.getString("difficulty")),
                            rs.getString("starter_code"),
                            rs.getBoolean("ai_solution_required"),
                            rs.getString("correct_answer")
                    );

                    questions.add(question);
                }
            }
        } catch (SQLException e) {
            log.error("Error getting questions for language {}: {}", languageId, e.getMessage());
            throw e;
        }

        return questions;
    }

    /**
     * gets all programming languages
     * @return list of all the programming languages in database
     * @throws SQLException error thrown if programming language doesn't exist
     */
    @Override
    public List<ProgrammingLanguage> getAllProgrammingLanguages() throws SQLException {
        List<ProgrammingLanguage> languages = new ArrayList<>();

        String sql = "SELECT * FROM programming_languages ORDER BY name";

        try (Connection conn = mySQLDao.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                languages.add(new ProgrammingLanguage(
                        rs.getInt("id"),
                        rs.getString("name")
                ));
            }
        } catch (SQLException e) {
            log.error("Error getting programming languages: {}", e.getMessage());
            throw e;
        }

        return languages;
    }
}