package codeify.repositories;

import codeify.entities.ProgrammingLanguage;
import codeify.entities.Question;
import codeify.persistance.QuestionRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class QuestionRepositoryTest {

    @InjectMocks
    private QuestionRepositoryImpl repositoryImpl;

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement statement;

    @Mock
    private ResultSet resultSet;

    private final int QUESTION_ID = 1;
    private final String TITLE = "Sample Question";
    private final String DESCRIPTION = "This is a sample question.";
    private final int LANGUAGE_ID = 10;
    private final String LANGUAGE_NAME = "Java";
    private final String Q_TYPE = Question.QuestionType.CODING.toString();
    private final String DIFFICULTY = Question.Difficulty.MEDIUM.toString();
    private final String STARTER_CODE = "public class Main {}";
    private final boolean AI_REQUIRED = false;
    private final String CORRECT_ANSWER = "42";
    private final Timestamp CREATED_AT = Timestamp.valueOf(LocalDateTime.of(2023, 3, 15, 12, 0));

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
    }

    /**
     * 1. Successfully get a question by its language
     * 2. Get an empty list of questions by its language
     * 3. SQLException when getting a question by its language
     */
    @Test
    void getQuestionByLanguage_Found() throws SQLException {
        String query = "SELECT q.*, pl.name as language_name FROM questions q JOIN programming_languages pl ON q.programming_language_id = pl.id WHERE pl.id = ? ORDER BY q.difficulty, q.id";
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("id")).thenReturn(QUESTION_ID, 2);
        when(resultSet.getString("title")).thenReturn(TITLE, "Another Question");
        when(resultSet.getString("description")).thenReturn(DESCRIPTION, "Another description");
        when(resultSet.getInt("programming_language_id")).thenReturn(LANGUAGE_ID, LANGUAGE_ID);
        when(resultSet.getString("language_name")).thenReturn(LANGUAGE_NAME, LANGUAGE_NAME);
        when(resultSet.getString("question_type")).thenReturn(Q_TYPE, Question.QuestionType.LOGIC.toString());
        when(resultSet.getString("difficulty")).thenReturn(DIFFICULTY, Question.Difficulty.HARD.toString());
        when(resultSet.getString("starter_code")).thenReturn(STARTER_CODE, "System.out.println(\"Hello\");");
        when(resultSet.getBoolean("ai_solution_required")).thenReturn(AI_REQUIRED, true);
        when(resultSet.getString("correct_answer")).thenReturn(CORRECT_ANSWER, "Hello World");
        when(resultSet.getTimestamp("created_at")).thenReturn(CREATED_AT, CREATED_AT);

        List<Question> questions = repositoryImpl.getQuestionByLanguage(LANGUAGE_ID);
        assertNotNull(questions);
        assertEquals(2, questions.size());

        Question question = questions.get(0);
        assertEquals(QUESTION_ID, question.getId());
        assertEquals(TITLE, question.getTitle());
        assertEquals(DESCRIPTION, question.getDescription());
        assertNotNull(question.getProgrammingLanguage());
        assertEquals(LANGUAGE_ID, question.getProgrammingLanguage().getId());
        assertEquals(Q_TYPE, question.getQuestionType().toString());
        assertEquals(DIFFICULTY, question.getDifficulty().toString());
        assertEquals(STARTER_CODE, question.getStarterCode());
        assertEquals(AI_REQUIRED, question.isAiSolutionRequired());
        assertEquals(CORRECT_ANSWER, question.getCorrectAnswer());
        assertEquals(CREATED_AT.toLocalDateTime(), question.getCreatedAt());

        question = questions.get(1);
        assertEquals(2, question.getId());
        assertEquals("Another Question", question.getTitle());
        assertEquals("Another description", question.getDescription());
        assertNotNull(question.getProgrammingLanguage());
        assertEquals(LANGUAGE_ID, question.getProgrammingLanguage().getId());
        assertEquals(Question.QuestionType.LOGIC.toString(), question.getQuestionType().toString());
        assertEquals(Question.Difficulty.HARD.toString(), question.getDifficulty().toString());
        assertEquals("System.out.println(\"Hello\");", question.getStarterCode());
        assertEquals(true, question.isAiSolutionRequired());
        assertEquals("Hello World", question.getCorrectAnswer());
        assertEquals(CREATED_AT.toLocalDateTime(), question.getCreatedAt());

        verify(connection).prepareStatement(query);
        verify(statement).setInt(1, LANGUAGE_ID);
        verify(statement).executeQuery();
    }
    @Test
    void getQuestionByLanguage_EmptyList() throws SQLException{
        String query = "SELECT q.*, pl.name as language_name FROM questions q JOIN programming_languages pl ON q.programming_language_id = pl.id WHERE pl.id = ? ORDER BY q.difficulty, q.id";
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        List<Question> questions = repositoryImpl.getQuestionByLanguage(LANGUAGE_ID);
        assertNotNull(questions);
        assertTrue(questions.isEmpty());

        verify(connection).prepareStatement(query);
        verify(statement).setInt(1, LANGUAGE_ID);
        verify(statement).executeQuery();
    }
    @Test
    void testGetQuestionByLanguage_SQLException() throws SQLException {
        String query = "SELECT q.*, pl.name as language_name FROM questions q JOIN programming_languages pl ON q.programming_language_id = pl.id WHERE pl.id = ? ORDER BY q.difficulty, q.id";
        when(statement.executeQuery()).thenThrow(new SQLException("Database error"));

        assertThrows(SQLException.class, () -> repositoryImpl.getQuestionByLanguage(LANGUAGE_ID));

        verify(connection).prepareStatement(query);
        verify(statement).setInt(1, LANGUAGE_ID);
        verify(statement).executeQuery();
    }

    /**
     * 1. Successfully get a question by its id
     * 2. Get an empty list of questions by its id
     * 3. SQLException when getting a question by its id
     */
    @Test
    void testGetQuestionById_Found() throws SQLException {
        String query = "SELECT q.*, pl.name as language_name FROM questions q JOIN programming_languages pl ON q.programming_language_id = pl.id WHERE q.id = ?";
        when(connection.prepareStatement(query)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("id")).thenReturn(QUESTION_ID);
        when(resultSet.getString("title")).thenReturn(TITLE);
        when(resultSet.getString("description")).thenReturn(DESCRIPTION);
        when(resultSet.getInt("programming_language_id")).thenReturn(LANGUAGE_ID);
        when(resultSet.getString("language_name")).thenReturn(LANGUAGE_NAME);
        when(resultSet.getString("question_type")).thenReturn(Q_TYPE);
        when(resultSet.getString("difficulty")).thenReturn(DIFFICULTY);
        when(resultSet.getString("starter_code")).thenReturn(STARTER_CODE);
        when(resultSet.getBoolean("ai_solution_required")).thenReturn(AI_REQUIRED);
        when(resultSet.getString("correct_answer")).thenReturn(CORRECT_ANSWER);
        when(resultSet.getTimestamp("created_at")).thenReturn(CREATED_AT);

        Question question = repositoryImpl.getQuestionById(QUESTION_ID);
        assertNotNull(question);
        assertEquals(QUESTION_ID, question.getId());
        assertEquals(TITLE, question.getTitle());
        assertEquals(DESCRIPTION, question.getDescription());
        assertNotNull(question.getProgrammingLanguage());
        assertEquals(LANGUAGE_ID, question.getProgrammingLanguage().getId());
        assertEquals(LANGUAGE_NAME, question.getProgrammingLanguage().getName());
        assertEquals(Q_TYPE, question.getQuestionType().toString());
        assertEquals(DIFFICULTY, question.getDifficulty().toString());
        assertEquals(STARTER_CODE, question.getStarterCode());
        assertEquals(AI_REQUIRED, question.isAiSolutionRequired());
        assertEquals(CORRECT_ANSWER, question.getCorrectAnswer());
        assertEquals(CREATED_AT.toLocalDateTime(), question.getCreatedAt());

        verify(statement).setInt(1, QUESTION_ID);
        verify(statement).executeQuery();
    }
    @Test
    void testGetQuestionById_NotFound() throws SQLException {
        String query = "SELECT q.*, pl.name as language_name FROM questions q JOIN programming_languages pl ON q.programming_language_id = pl.id WHERE q.id = ?";
        when(connection.prepareStatement(query)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Question question = repositoryImpl.getQuestionById(QUESTION_ID);
        assertNull(question);

        verify(statement).setInt(1, QUESTION_ID);
        verify(statement).executeQuery();
    }
    @Test
    void testGetQuestionById_SQLException() throws SQLException {
        String query = "SELECT q.*, pl.name as language_name FROM questions q JOIN programming_languages pl ON q.programming_language_id = pl.id WHERE q.id = ?";
        when(connection.prepareStatement(query)).thenReturn(statement);
        when(statement.executeQuery()).thenThrow(new SQLException("Database error"));

        assertThrows(SQLException.class, () -> repositoryImpl.getQuestionById(QUESTION_ID));

        verify(statement).setInt(1, QUESTION_ID);
        verify(statement).executeQuery();
    }

    /**
     * 1. Successfully add a question
     * 2. Fails to add a question
     * 3. SQLException has occurred
     */
    @Test
    void testAddQuestion_Success() throws SQLException {
        String query = "INSERT INTO questions (title, description, programming_language_id, question_type, difficulty, starter_code, ai_solution_required, correct_answer) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        ProgrammingLanguage programmingLanguage = new ProgrammingLanguage();
        programmingLanguage.setId(LANGUAGE_ID);
        Question question = Question.builder()
                .title(TITLE)
                .description(DESCRIPTION)
                .programmingLanguage(programmingLanguage)
                .questionType(Question.QuestionType.valueOf(Q_TYPE))
                .difficulty(Question.Difficulty.valueOf(DIFFICULTY))
                .starterCode(STARTER_CODE)
                .aiSolutionRequired(AI_REQUIRED)
                .correctAnswer(CORRECT_ANSWER)
                .build();
        when(connection.prepareStatement(query)).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);

        boolean result = repositoryImpl.addQuestion(question);
        assertTrue(result);

        verify(statement).setString(1, TITLE);
        verify(statement).setString(2, DESCRIPTION);
        verify(statement).setInt(3, LANGUAGE_ID);
        verify(statement).setString(4, Q_TYPE);
        verify(statement).setString(5, DIFFICULTY);
        verify(statement).setString(6, STARTER_CODE);
        verify(statement).setBoolean(7, AI_REQUIRED);
        verify(statement).setString(8, CORRECT_ANSWER);
        verify(statement).executeUpdate();
    }
    @Test
    void testAddQuestion_NoSuccess() throws SQLException {
        String query = "INSERT INTO questions (title, description, programming_language_id, question_type, difficulty, starter_code, ai_solution_required, correct_answer) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        ProgrammingLanguage programmingLanguage = new ProgrammingLanguage();
        programmingLanguage.setId(LANGUAGE_ID);
        Question question = Question.builder()
                .title(TITLE)
                .description(DESCRIPTION)
                .programmingLanguage(programmingLanguage)
                .questionType(Question.QuestionType.valueOf(Q_TYPE))
                .difficulty(Question.Difficulty.valueOf(DIFFICULTY))
                .starterCode(STARTER_CODE)
                .aiSolutionRequired(AI_REQUIRED)
                .correctAnswer(CORRECT_ANSWER)
                .build();
        when(connection.prepareStatement(query)).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(0);

        boolean result = repositoryImpl.addQuestion(question);
        assertFalse(result);

        // Verify the method calls
        verify(statement).setString(1, TITLE);
        verify(statement).setString(2, DESCRIPTION);
        verify(statement).setInt(3, LANGUAGE_ID);
        verify(statement).setString(4, Q_TYPE);
        verify(statement).setString(5, DIFFICULTY);
        verify(statement).setString(6, STARTER_CODE);
        verify(statement).setBoolean(7, AI_REQUIRED);
        verify(statement).setString(8, CORRECT_ANSWER);
        verify(statement).executeUpdate();
    }
    @Test
    void testAddQuestion_SQLException() throws SQLException {
        // Set up the test case
        String query = "INSERT INTO questions (title, description, programming_language_id, question_type, difficulty, starter_code, ai_solution_required, correct_answer) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        ProgrammingLanguage programmingLanguage = new ProgrammingLanguage();
        programmingLanguage.setId(LANGUAGE_ID);
        Question question = Question.builder()
                .title(TITLE)
                .description(DESCRIPTION)
                .programmingLanguage(programmingLanguage)
                .questionType(Question.QuestionType.valueOf(Q_TYPE))
                .difficulty(Question.Difficulty.valueOf(DIFFICULTY))
                .starterCode(STARTER_CODE)
                .aiSolutionRequired(AI_REQUIRED)
                .correctAnswer(CORRECT_ANSWER)
                .build();
        when(connection.prepareStatement(query)).thenReturn(statement);
        when(statement.executeUpdate()).thenThrow(new SQLException("Database error"));

        assertThrows(SQLException.class, () -> repositoryImpl.addQuestion(question));

        // Verify the method calls
        verify(statement).setString(1, TITLE);
        verify(statement).setString(2, DESCRIPTION);
        verify(statement).setInt(3, LANGUAGE_ID);
        verify(statement).setString(4, Q_TYPE);
        verify(statement).setString(5, DIFFICULTY);
        verify(statement).setString(6, STARTER_CODE);
        verify(statement).setBoolean(7, AI_REQUIRED);
        verify(statement).setString(8, CORRECT_ANSWER);
        verify(statement).executeUpdate();
    }

    /**
     * 1. Successfully update a question
     * 2. Fails to update a question
     * 3. SQLException has occurred
     */
    @Test
    void testUpdateQuestion_Success() throws SQLException {
        String query = "UPDATE questions SET title = ?, description = ?, programming_language_id = ?, question_type = ?, difficulty = ?, starter_code = ?, ai_solution_required = ?, correct_answer = ? WHERE id = ?";
        ProgrammingLanguage programmingLanguage = new ProgrammingLanguage();
        programmingLanguage.setId(LANGUAGE_ID);
        Question question = Question.builder()
                .id(QUESTION_ID)
                .title(TITLE)
                .description(DESCRIPTION)
                .programmingLanguage(programmingLanguage)
                .questionType(Question.QuestionType.valueOf(Q_TYPE))
                .difficulty(Question.Difficulty.valueOf(DIFFICULTY))
                .starterCode(STARTER_CODE)
                .aiSolutionRequired(AI_REQUIRED)
                .correctAnswer(CORRECT_ANSWER)
                .build();
        when(connection.prepareStatement(query)).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);

        boolean result = repositoryImpl.updateQuestion(question);
        assertTrue(result);

        verify(statement).setString(1, TITLE);
        verify(statement).setString(2, DESCRIPTION);
        verify(statement).setInt(3, LANGUAGE_ID);
        verify(statement).setString(4, Q_TYPE);
        verify(statement).setString(5, DIFFICULTY);
        verify(statement).setString(6, STARTER_CODE);
        verify(statement).setBoolean(7, AI_REQUIRED);
        verify(statement).setString(8, CORRECT_ANSWER);
        verify(statement).setInt(9, QUESTION_ID);
        verify(statement).executeUpdate();
    }
    @Test
    void testUpdateQuestion_NoSuccess() throws SQLException {
        String query = "UPDATE questions SET title = ?, description = ?, programming_language_id = ?, question_type = ?, difficulty = ?, starter_code = ?, ai_solution_required = ?, correct_answer = ? WHERE id = ?";
        ProgrammingLanguage programmingLanguage = new ProgrammingLanguage();
        programmingLanguage.setId(LANGUAGE_ID);
        Question question = Question.builder()
                .id(QUESTION_ID)
                .title(TITLE)
                .description(DESCRIPTION)
                .programmingLanguage(programmingLanguage)
                .questionType(Question.QuestionType.valueOf(Q_TYPE))
                .difficulty(Question.Difficulty.valueOf(DIFFICULTY))
                .starterCode(STARTER_CODE)
                .aiSolutionRequired(AI_REQUIRED)
                .correctAnswer(CORRECT_ANSWER)
                .build();
        when(connection.prepareStatement(query)).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(0);

        boolean result = repositoryImpl.updateQuestion(question);
        assertFalse(result);

        verify(statement).setString(1, TITLE);
        verify(statement).setString(2, DESCRIPTION);
        verify(statement).setInt(3, LANGUAGE_ID);
        verify(statement).setString(4, Q_TYPE);
        verify(statement).setString(5, DIFFICULTY);
        verify(statement).setString(6, STARTER_CODE);
        verify(statement).setBoolean(7, AI_REQUIRED);
        verify(statement).setString(8, CORRECT_ANSWER);
        verify(statement).setInt(9, QUESTION_ID);
        verify(statement).executeUpdate();
    }
    @Test
    void testUpdateQuestion_SQLException() throws SQLException {
        String query = "UPDATE questions SET title = ?, description = ?, programming_language_id = ?, question_type = ?, difficulty = ?, starter_code = ?, ai_solution_required = ?, correct_answer = ? WHERE id = ?";
        ProgrammingLanguage programmingLanguage = new ProgrammingLanguage();
        programmingLanguage.setId(LANGUAGE_ID);
        Question question = Question.builder()
                .id(QUESTION_ID)
                .title(TITLE)
                .description(DESCRIPTION)
                .programmingLanguage(programmingLanguage)
                .questionType(Question.QuestionType.valueOf(Q_TYPE))
                .difficulty(Question.Difficulty.valueOf(DIFFICULTY))
                .starterCode(STARTER_CODE)
                .aiSolutionRequired(AI_REQUIRED)
                .correctAnswer(CORRECT_ANSWER)
                .build();
        when(connection.prepareStatement(query)).thenReturn(statement);
        when(statement.executeUpdate()).thenThrow(new SQLException("Database error"));

        assertThrows(SQLException.class, () -> repositoryImpl.updateQuestion(question));

        verify(statement).setString(1, TITLE);
        verify(statement).setString(2, DESCRIPTION);
        verify(statement).setInt(3, LANGUAGE_ID);
        verify(statement).setString(4, Q_TYPE);
        verify(statement).setString(5, DIFFICULTY);
        verify(statement).setString(6, STARTER_CODE);
        verify(statement).setBoolean(7, AI_REQUIRED);
        verify(statement).setString(8, CORRECT_ANSWER);
        verify(statement).setInt(9, QUESTION_ID);
        verify(statement).executeUpdate();
    }

    /**
     * 1. Successfully delete a question
     * 2. Fails to delete a question
     * 3. SQLException has occurred
     */
    @Test
    void testDeleteQuestion_Success() throws SQLException {
        String query = "DELETE FROM questions WHERE id = ?";
        when(connection.prepareStatement(query)).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);

        boolean result = repositoryImpl.deleteQuestion(QUESTION_ID);
        assertTrue(result);

        verify(statement).setInt(1, QUESTION_ID);
        verify(statement).executeUpdate();
    }
    @Test
    void testDeleteQuestion_NoSuccess() throws SQLException {
        String query = "DELETE FROM questions WHERE id = ?";
        when(connection.prepareStatement(query)).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(0);

        boolean result = repositoryImpl.deleteQuestion(QUESTION_ID);
        assertFalse(result);

        verify(statement).setInt(1, QUESTION_ID);
        verify(statement).executeUpdate();
    }
    @Test
    void testDeleteQuestion_SQLException() throws SQLException {
        String query = "DELETE FROM questions WHERE id = ?";
        when(connection.prepareStatement(query)).thenReturn(statement);
        when(statement.executeUpdate()).thenThrow(new SQLException("Database error"));

        assertThrows(SQLException.class, () -> repositoryImpl.deleteQuestion(QUESTION_ID));

        verify(statement).setInt(1, QUESTION_ID);
        verify(statement).executeUpdate();
    }

    /**
     * 1. Successfully get a list of questions
     * 2. Get an empty list of questions
     * 3. SQLException has occurred
     */
    @Test
    void testGetQuestions_Success() throws SQLException {
        String query = "SELECT * FROM questions ORDER BY title";
        when(connection.prepareStatement(query)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("id")).thenReturn(QUESTION_ID, 2);
        when(resultSet.getString("title")).thenReturn(TITLE, "Another Question");
        when(resultSet.getString("description")).thenReturn(DESCRIPTION, "Another description");
        when(resultSet.getInt("programming_language_id")).thenReturn(LANGUAGE_ID, LANGUAGE_ID);
        when(resultSet.getString("language_name")).thenReturn(LANGUAGE_NAME, LANGUAGE_NAME);
        when(resultSet.getString("question_type")).thenReturn(Q_TYPE, Question.QuestionType.LOGIC.toString());
        when(resultSet.getString("difficulty")).thenReturn(DIFFICULTY, Question.Difficulty.HARD.toString());
        when(resultSet.getString("starter_code")).thenReturn(STARTER_CODE, "System.out.println(\"Hello\");");
        when(resultSet.getBoolean("ai_solution_required")).thenReturn(AI_REQUIRED, true);
        when(resultSet.getString("correct_answer")).thenReturn(CORRECT_ANSWER, "Hello World");
        when(resultSet.getTimestamp("created_at")).thenReturn(CREATED_AT, CREATED_AT);

        List<Question> questions = repositoryImpl.getQuestions();
        assertNotNull(questions);
        assertEquals(2, questions.size());

        Question question = questions.get(0);
        assertEquals(QUESTION_ID, question.getId());
        assertEquals(TITLE, question.getTitle());
        assertEquals(DESCRIPTION, question.getDescription());
        assertNotNull(question.getProgrammingLanguage());
        assertEquals(LANGUAGE_ID, question.getProgrammingLanguage().getId());
        assertEquals(Q_TYPE, question.getQuestionType().toString());
        assertEquals(DIFFICULTY, question.getDifficulty().toString());
        assertEquals(STARTER_CODE, question.getStarterCode());
        assertEquals(AI_REQUIRED, question.isAiSolutionRequired());
        assertEquals(CORRECT_ANSWER, question.getCorrectAnswer());
        assertEquals(CREATED_AT.toLocalDateTime(), question.getCreatedAt());

        question = questions.get(1);
        assertEquals(2, question.getId());
        assertEquals("Another Question", question.getTitle());
        assertEquals("Another description", question.getDescription());
        assertNotNull(question.getProgrammingLanguage());
        assertEquals(LANGUAGE_ID, question.getProgrammingLanguage().getId());
        assertEquals(Question.QuestionType.LOGIC.toString(), question.getQuestionType().toString());
        assertEquals(Question.Difficulty.HARD.toString(), question.getDifficulty().toString());
        assertEquals("System.out.println(\"Hello\");", question.getStarterCode());
        assertEquals(true, question.isAiSolutionRequired());
        assertEquals("Hello World", question.getCorrectAnswer());
        assertEquals(CREATED_AT.toLocalDateTime(), question.getCreatedAt());

        verify(connection).prepareStatement(query);
        verify(statement).executeQuery();
    }
    @Test
    void testGetQuestions_EmptyList() throws SQLException {
        String query = "SELECT * FROM questions ORDER BY title";
        when(connection.prepareStatement(query)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        List<Question> questions = repositoryImpl.getQuestions();
        assertNotNull(questions);
        assertTrue(questions.isEmpty());

        verify(connection).prepareStatement(query);
        verify(statement).executeQuery();
    }
    @Test
    void testGetQuestions_SQLException() throws SQLException {
        String query = "SELECT * FROM questions ORDER BY title";
        when(connection.prepareStatement(query)).thenReturn(statement);
        when(statement.executeQuery()).thenThrow(new SQLException("Database error"));

        assertThrows(SQLException.class, () -> repositoryImpl.getQuestions());

        verify(connection).prepareStatement(query);
        verify(statement).executeQuery();
    }
}