package codeify.controllers;

import codeify.model.ProgrammingLanguage;
import codeify.persistance.ProgrammingLanguageRepositoryImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProgrammingLanguageControllerTest {

    @InjectMocks
    private ProgrammingLanguageController programmingLanguageController;

    @Mock
    private ProgrammingLanguageRepositoryImpl programmingLanguageRepositoryImpl;

    /**
     * Test Case 1: Valid response with data - status 200 OK
     *
     * @throws SQLException Database error
     */
    @Test
    public void testShowLanguages_Success() throws SQLException {
        // Mock Data
        List<ProgrammingLanguage> mockLanguages = Arrays.asList(
                new ProgrammingLanguage(1, "Java"),
                new ProgrammingLanguage(2, "Python")
        );

        // Mocking an instance
        when(programmingLanguageRepositoryImpl.getAllProgrammingLanguage()).thenReturn(mockLanguages);

        // Call the controller method
        ResponseEntity<?> response = programmingLanguageController.showLanguages();

        // Assertions
        assertEquals(200, response.getStatusCode().value());
        assertEquals(mockLanguages, response.getBody());

        // Verify that the method was called once
        verify(programmingLanguageRepositoryImpl, times(1)).getAllProgrammingLanguage();
    }

    /**
     * Test Case 2: Empty list response - status 204 No Content
     *
     * @throws SQLException Database error
     */
    @Test
    public void testShowLanguages_EmptyList() throws SQLException {

        // Mocks empty list of languages
        when(programmingLanguageRepositoryImpl.getAllProgrammingLanguage()).thenReturn(Collections.emptyList());

        // Call the controller method
        ResponseEntity<?> response = programmingLanguageController.showLanguages();

        // Assertions
        assertEquals(204, response.getStatusCode().value());
        assertEquals("No programming languages has been found.", response.getBody());

        // Verify that the method was called once
        verify(programmingLanguageRepositoryImpl, times(1)).getAllProgrammingLanguage();
    }

    /**
     * Test Case 3: Database Error Handling - status 500 SQL Server Error
     *
     * @throws SQLException Database error
     */
    @Test
    public void testShowLanguages_DatabaseError() throws SQLException {

        // Mocks SQL database error
        when(programmingLanguageRepositoryImpl.getAllProgrammingLanguage()).thenThrow(new SQLException("Database error"));

        // Call the controller method
        ResponseEntity<?> response = programmingLanguageController.showLanguages();

        // Assertions
        assertEquals(500, response.getStatusCode().value());
        assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("Error retrieving languages"));

        // Verify that the method was called once
        verify(programmingLanguageRepositoryImpl, times(1)).getAllProgrammingLanguage();
    }

    /**
     * Test Case 4: Null returned - status 500 Internal server error
     *
     * @throws SQLException Database error
     */
    @Test
    public void testShowLanguages_NullReturned() throws SQLException {

        // Mocks null list of languages
        when(programmingLanguageRepositoryImpl.getAllProgrammingLanguage()).thenReturn(null);

        // Call the controller method
        ResponseEntity<?> response = programmingLanguageController.showLanguages();

        // Assertions
        assertEquals(500, response.getStatusCode().value());
        assertEquals("Retrieval of languages has failed.", response.getBody());

        // Verify that the method was called once
        verify(programmingLanguageRepositoryImpl, times(1)).getAllProgrammingLanguage();
    }

    /**
     * Test Case 5: List with null returned - status 500 Internal server error
     *
     * @throws SQLException Database error
     */
    @Test
    public void testShowLanguages_ListWithNullValues() throws SQLException {

        // Mock data
        List<ProgrammingLanguage> languages = Arrays.asList(
                new ProgrammingLanguage(1, "Java"),
                null,
                new ProgrammingLanguage(2, "Python")
        );

        // Mocks list of languages containing a null
        when(programmingLanguageRepositoryImpl.getAllProgrammingLanguage()).thenReturn(languages);

        // Call the controller method
        ResponseEntity<?> response = programmingLanguageController.showLanguages();

        // Assertions
        assertEquals(500, response.getStatusCode().value());
        assertEquals("Retrieval of languages has failed: List contains null values.", response.getBody());

        // Verify that the method was called once
        verify(programmingLanguageRepositoryImpl, times(1)).getAllProgrammingLanguage();
    }

    /**
     * Test Case 6: Large list of languages - status 200 OK
     *
     * @throws SQLException Database error
     */
    @Test
    public void testShowLanguages_LargeList() throws SQLException {

        //Mock data
        List<ProgrammingLanguage> languages = IntStream.range(0, 1000)
                .mapToObj(i -> new ProgrammingLanguage(i, "Language " + 1))
                .collect(Collectors.toList());

        // Mocks large list of languages
        when(programmingLanguageRepositoryImpl.getAllProgrammingLanguage()).thenReturn(languages);

        // Call the controller method
        ResponseEntity<?> response = programmingLanguageController.showLanguages();

        // Assertions
        assertEquals(200, response.getStatusCode().value());
        assertEquals(languages, response.getBody());

        // Verify that the method was called once
        verify(programmingLanguageRepositoryImpl, times(1)).getAllProgrammingLanguage();
    }

    /**
     * Test Case 7: Custom Runtime exception - HTTP status INTERNAL_SERVER_ERROR
     *
     * @throws SQLException  Database error
     */
    @Test
    void testShowLanguages_CustomRuntimeException() throws SQLException {

        // Mocks a custom Runtime exception
        when(programmingLanguageRepositoryImpl.getAllProgrammingLanguage()).thenThrow(new RuntimeException("Custom error"));

        // Call the controller method
        ResponseEntity<?> response = programmingLanguageController.showLanguages();

        // Assertions
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("Error retrieving languages: Custom error"));

        // Verify that the method was called once
        verify(programmingLanguageRepositoryImpl, times(1)).getAllProgrammingLanguage();
    }

    /**
     * Test Case 8: Tests Iteration - method is called only once
     *
     * @throws SQLException Database error
     */
    @Test
    void testShowLanguages_VerifyRepositoryInteraction() throws SQLException {

        // Mock data
        List<ProgrammingLanguage> languages = Arrays.asList(
                new ProgrammingLanguage(1, "Java"),
                new ProgrammingLanguage(2, "Python")
        );

        // Mocks a list of languages
        when(programmingLanguageRepositoryImpl.getAllProgrammingLanguage()).thenReturn(languages);

        // Verify that the method was called once
        verify(programmingLanguageRepositoryImpl, times(1)).getAllProgrammingLanguage();
    }
}
