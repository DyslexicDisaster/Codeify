package codeify.controllers;

import codeify.persistance.UserRepositoryImpl;
import codeify.model.*;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserRepositoryImpl userRepositoryImpl;

    @Mock
    private HttpSession session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 1. Empty Fields - 400 Bad Request - "All fields must be filled out"
     * 2. Successful Registration - 201 Created - Success message
     * 3. Username Exists - 409 Conflict - "Username is already taken!"
     * 4. Email Exists - 409 Conflict - "Email is already taken!"
     * 5. Database Error - 500 Internal Server Error - "Database error"
     * 6. Password Hashing Error - 500 Internal Server Error - "Password hashing error"
     */

    /**
     * Test Case 1: Register a new user with empty fields - status 400 Bad Request
     */
    @Test
    void testRegisterUser_EmptyParameters() {

        // Set the parameters
        String username = "";
        String password = "";
        String email = "";

        // Call the method
        ResponseEntity<?> response = userController.registerUser(username, password, email);

        // Check if the response is correct
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("All fields must be filled out", response.getBody());
    }

    /**
     * Test Case 2: Register successfully a new user - status 201 Ok
     */
    @Test
    void testRegisterUser_Success() throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        // Implement the test case for a successful registration
    }

    /**
     * Test Case 3: Register a new user with an already existing username - status 409 Conflict
     */
    @Test
    void testRegisterUser_UsernameAlreadyExists() throws SQLException {
        // Set the parameters
        String username = "test";
        String password = "testOne123**";
        String email = "testing@test.com";

        // Mock the database
        when(userRepositoryImpl.existsByUsername(username)).thenReturn(true);

        // Call the method
        ResponseEntity<?> response = userController.registerUser(username, password, email);

        // Check if the response is correct
        assertEquals(409, response.getStatusCodeValue());
        assertEquals("Username is already taken!", response.getBody());
    }

    /**
     * Test Case 4: Register a new user with an already existing email - status 409 Conflict
     */
    @Test
    void testRegisterUser_EmailAlreadyExists() throws SQLException {
        // Set the parameters
        String username = "test";
        String password = "testOne123**";
        String email = "testing@test.com";

        // Mock the database
        when(userRepositoryImpl.existsByUsername(username)).thenReturn(false);
        when(userRepositoryImpl.existsByEmail(email)).thenReturn(true);

        // Call the method
        ResponseEntity<?> response = userController.registerUser(username, password, email);

        // Check if the response is correct
        assertEquals(409, response.getStatusCodeValue());
        assertEquals("Email is already taken!", response.getBody());
    }

    /**
     * Test Case 5: Register a new user with an error in the database - status 500 Internal Server Error
     */
    @Test
    void testRegisterUser_DatabaseError() throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {

        // Set the parameters
        String username = "test";
        String password = "testOne123**";
        String email = "testing@test.com";

        // Mock the database
        when(userRepositoryImpl.existsByUsername(username)).thenReturn(false);
        when(userRepositoryImpl.existsByEmail(email)).thenReturn(false);

        // Mock the database error
        when(userRepositoryImpl.register(any())).thenThrow(new SQLException("Database error"));

        // Call the method
        ResponseEntity<?> response = userController.registerUser(username, password, email);

        // Check if the response is correct
        assertEquals(500, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("An error has occured during registration"));
    }

    /**
     * Test Case 6: Register a new user with an error in the password hashing - status 500 Internal Server Error
     */
    @Test
    void testRegisterUser_PasswordHashingError() throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {

        // Set the parameters
        String username = "test";
        String password = "testOne123**";
        String email = "testing@test.com";

        // Mock the database
        when(userRepositoryImpl.existsByUsername(username)).thenReturn(false);
        when(userRepositoryImpl.existsByEmail(email)).thenReturn(false);

        // Mock the password hashing error
        when(userRepositoryImpl.register(any())).thenThrow(new NoSuchAlgorithmException("Password hashing error"));

        // Call the method
        ResponseEntity<?> response = userController.registerUser(username, password, email);

        // Check if the response is correct
        assertEquals(500, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("An error has occured during registration"));
    }

    /**
     * 1. Empty Fields - 400 Bad Request - "All fields must be filled out"
     * 2. Successful Login - 200 Ok - "Login successful"
     * 3. Login returns null - 401 Unauthorized - "Invalid username/password combination"
     * 4. SQLException occurs - 500 Internal error - "An error occurred during login"
     */

    /*
     * Test Case 1: Login with empty fields - status 400 Bad Request
     */
    @Test
    void testLoginUser_EmptyParameters() {

        // Set the parameters
        String username = "";
        String password = "";

        // Call the method
        ResponseEntity<?> response = userController.loginUser(username, password, session);

        // Check if the response is correct
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("All fields must be filled out", response.getBody());
    }

    /*
     * Test Case 2: Login with valid credentials - status 200 Ok
     */
    @Test
    void testLoginUser_Success() throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {

        // Set the parameters
        String username = "test";
        String password = "testOne123**";

        // Mock the database
        when(userRepositoryImpl.login(username, password)).thenReturn(new User(username, password, "salt", "email", LocalDate.now()));

        // Call the method
        ResponseEntity<?> response = userController.loginUser(username, password, session);

        // Check if the response is correct
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Login successful", responseBody.get("message"));
        assertEquals(username, responseBody.get("username"));
    }

    /**
     * Test Case 3: Login with invalid credentials - status 401 Unauthorized
     */
    @Test
    void testLoginUser_InvalidLogin() throws SQLException {
        // Set the parameters
        String username = "test";
        String password = "testOne123**";

        // Mock the database
        when(userRepositoryImpl.login(username, password)).thenReturn(null);

        // Call the method
        ResponseEntity<?> response = userController.loginUser(username, password, session);

        // Check if the response is correct
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Invalid username/password combination", response.getBody());
    }

    /*
     * Test Case 4: SQLException has occurred - status 500 Internal Server Error
     */
    @Test
    void testLoginUser_SqlException() throws SQLException {
        // Set the parameters
        String username = "test";
        String password = "testOne123**";

        // Mock the database
        when(userRepositoryImpl.login(username, password)).thenThrow(new SQLException("Database error"));

        // Call the method
        ResponseEntity<?> response = userController.loginUser(username, password, session);

        // Check if the response is correct
        assertEquals(500, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("An error occurred during login"));
    }

    /**
     * 1. User is logged in - 200 OK - "Logout successful for user: username"
     * 2. No user is logged in - 200 OK - "No user is currently logged in"
     */

    /**
     * Test Case 1: User is logged in - status 200 Ok
     */
    @Test
    void testLogoutUser_UserLoggedIn() {
        // Set the session
        User user = new User("test", "testOne123**", "salt", "email", LocalDate.now());
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("loggedInUser")).thenReturn(user);

        // Call the method
        ResponseEntity<?> response = userController.logout(session);

        // Check if the response is correct
        assertEquals(200, response.getStatusCodeValue());

        // Check if the response body is correct
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("Logout successful for user: test", responseBody.get("message"));

        // Verify that the method was called once
        verify(session, times(1)).getAttribute("loggedInUser");
    }

    /**
     * Test Case 2: No user is logged in - status 200 Ok
     */
    @Test
    void testLogoutUser_NoUserLoggedIn() {
        // Set the session
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("loggedInUser")).thenReturn(null);

        // Call the method
        ResponseEntity<?> response = userController.logout(session);

        // Check if the response is correct
        assertEquals(200, response.getStatusCodeValue());

        // Check if the response body is correct
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("No user is currently logged in", responseBody.get("message"));

        // Verify that the method was called once
        verify(session, times(1)).getAttribute("loggedInUser");
    }
}