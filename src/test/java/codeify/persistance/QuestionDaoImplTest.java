package codeify.persistance;

import codeify.business.Question;
import codeify.business.ProgrammingLanguage;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for QuestionDaoImpl. Tests functionality for questions
 * and programming languages.
 */
class QuestionDaoImplTest {
    private static final String TEST_PROPERTIES = "database_test.properties";
    private QuestionDao questionDao;
    private static MySQLDao mySQLDao;

    @BeforeAll
    static void setUp() {
        mySQLDao = new MySQLDao(TEST_PROPERTIES);
    }

    @BeforeEach
    void setUpEach() throws SQLException {
        questionDao = new QuestionDaoImpl(TEST_PROPERTIES);
        setupTestData();
    }

    /**
     * Sets up test data in the database before each test
     */
    private void setupTestData() throws SQLException {
        try (Connection conn = mySQLDao.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("DELETE FROM questions");
            stmt.execute("DELETE FROM programming_languages");

            stmt.execute("INSERT INTO programming_languages (id, name) VALUES (1, 'Java')");
            stmt.execute("INSERT INTO programming_languages (id, name) VALUES (2, 'JavaScript')");

            String insertQuestion = """
                INSERT INTO questions (id, title, description, programming_language_id, 
                question_type, difficulty, starter_code, ai_solution_required, correct_answer)
                VALUES 
                (1, 'Java Basics', 'Learn Java fundamentals', 1, 'CODING', 'EASY', 
                'public class Main {}', false, null),
                (2, 'Java OOP', 'Object-oriented programming', 1, 'CODING', 'MEDIUM', 
                'public class OOP {}', false, null),
                (3, 'Js Basics', 'Learn JS fundamentals', 2, 'CODING', 'EASY',
                                    '<script>
                 document.getElementById("demo").innerHTML = 
                                    </script>', false, null)
                """;
            stmt.execute(insertQuestion);
        }
    }

    //this tests to see if all java questions are returned when id is 1 or correct id for java
    @Test
    void getQuestionsByLanguage_ShouldReturnJavaQuestions_WhenLanguageIdIsOne() throws SQLException {
        List<Question> javaQuestions = questionDao.getQuestionsByLanguage(1);

        assertEquals(2, javaQuestions.size(), "Should return 2 Java questions");
        assertTrue(javaQuestions.stream()
                        .allMatch(q -> q.getProgrammingLanguage().getName().equals("Java")),
                "All questions should be Java questions");
    }

    //this tests to see if all java questions are returned when id is 2 or correct id for python
    @Test
    void getQuestionsByLanguage_ShouldReturnJavaScriptQuestions_WhenLanguageIdIsTwo() throws SQLException {
        List<Question> javaScriptQuestions = questionDao.getQuestionsByLanguage(2);

        assertEquals(1, javaScriptQuestions.size(), "Should return 1 JavaScript question");
        assertTrue(javaScriptQuestions.stream()
                        .allMatch(q -> q.getProgrammingLanguage().getName().equals("JavaScript")),
                "All questions should be JavaScript questions");
    }

    //this tests to see if list returned is empty if invalid id is entered
    @Test
    void getQuestionsByLanguage_ShouldReturnEmptyList_WhenLanguageDoesNotExist() throws SQLException {
        List<Question> questions = questionDao.getQuestionsByLanguage(999);

        assertTrue(questions.isEmpty(), "Should return empty list for non-existent language");
    }

    //test to see if all languages are correctly returned
    @Test
    void getAllProgrammingLanguages_ShouldReturnAllLanguages() throws SQLException {
        List<ProgrammingLanguage> languages = questionDao.getAllProgrammingLanguages();
        assertEquals(2, languages.size(), "Should return 2 programming languages");
        assertTrue(languages.stream()
                        .anyMatch(lang -> lang.getName().equals("Java")),
                "Should contain Java");
        assertTrue(languages.stream()
                        .anyMatch(lang -> lang.getName().equals("JavaScript")),
                "Should contain JavaScript");
    }

    //after each run delete questions and programming languages
    @AfterEach
    void delete() throws SQLException {
        try (Connection conn = mySQLDao.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM questions");
            stmt.execute("DELETE FROM programming_languages");
        }
    }
}