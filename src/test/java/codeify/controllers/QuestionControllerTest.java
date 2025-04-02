package codeify.controllers;

import codeify.controllers.api.QuestionController;
import codeify.entities.ProgrammingLanguage;
import codeify.entities.Question;
import codeify.persistance.implementations.QuestionRepositoryImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuestionControllerTest {

    @InjectMocks
    private QuestionController questionController;

    @Mock
    private QuestionRepositoryImpl questionRepositoryImpl;

    /**
     * 1. Language ID is null - 400 Bad Request - "ID of language cannot be null"
     * 2. Retrieves an empty list - 404 Not Found - "Error retrieving questions: List is empty"
     * 3. Retrieves a list of languages - 200 OK - List of languages
     * 4. SQLException has occurred - 500 Internal Error - "Error retrieving questions"
     */

    /**
     * Test Case 1: Null language ID - status 400 Bad Request
     *
     * @throws SQLException Database error
     */
    @Test
    public void testShowQuestions_NullLanguageId() throws SQLException {

        //Mock data
        Integer languageId = null;

        // Call the controller method
        ResponseEntity<?> response = questionController.showQuestions(languageId);

        // Assertions
        assertEquals(400, response.getStatusCode().value());
        assertEquals("ID of language cannot be null", response.getBody());

        // Verify that the method was called once
        verify(questionRepositoryImpl, never()).getQuestionByLanguage(anyInt());
    }

    /**
     * Test Case 2: Empty list of questions - status 500 Internal Server Error
     *
     * @throws SQLException Database error
     */
    @Test
    public void testShowQuestions_EmptyList() throws SQLException {

        // Mocking an empty list
        List<Question> mockQuestions = Arrays.asList();

        // Mocking the method
        when(questionRepositoryImpl.getQuestionByLanguage(1)).thenReturn(mockQuestions);

        // Call the controller method
        ResponseEntity<?> response = questionController.showQuestions(1);

        // Assertions
        assertEquals(404, response.getStatusCode().value());
        assertEquals("Error retrieving questions: List is empty", response.getBody());

        // Verify that the method was called once
        verify(questionRepositoryImpl, times(1)).getQuestionByLanguage(1);
    }

    /**
     * Test Case 3: Valid language ID - status 200 OK
     *
     * @throws SQLException Database error
     */
    @Test
    public void testShowQuestions_ValidLanguageId() throws SQLException {

        // Mock Data
        Question question = Question.builder().
                id(1).
                title("Test Question").
                description("Test Description").
                programmingLanguage(ProgrammingLanguage.builder().id(1).name("Java").build()).
                questionType(Question.QuestionType.CODING).
                difficulty(Question.Difficulty.EASY).
                starterCode("starter code").
                aiSolutionRequired(false).
                correctAnswer("correct answer").
                createdAt(LocalDateTime.now()).
                build();

        // Mocking an instance
        List<Question> mockQuestions = Arrays.asList(question);

        // Mocking the method
        when(questionRepositoryImpl.getQuestionByLanguage(1)).thenReturn(mockQuestions);

        // Call the controller method
        ResponseEntity<?> response = questionController.showQuestions(1);

        // Assertions
        assertEquals(200, response.getStatusCode().value());
        assertEquals(mockQuestions, response.getBody());

        // Verify that the method was called once
        verify(questionRepositoryImpl, times(1)).getQuestionByLanguage(1);
    }

    /**
     * Test Case 4: SQLException has occurred - status 500 Internal Server Error
     *
     * @throws SQLException Database error
     */
    @Test
    public void testShowQuestions_DatabaseError() throws SQLException {

        // Mocking the method
        when(questionRepositoryImpl.getQuestionByLanguage(1)).thenThrow(new SQLException("Database error"));

        // Call the controller method
        ResponseEntity<?> response = questionController.showQuestions(1);

        // Assertions
        assertEquals(500, response.getStatusCode().value());
        assertEquals("Error retrieving questions: Database error", response.getBody());

        // Verify that the method was called once
        verify(questionRepositoryImpl, times(1)).getQuestionByLanguage(1);
    }
}