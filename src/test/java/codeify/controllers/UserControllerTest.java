package codeify.controllers;

import codeify.persistance.UserRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import codeify.util.*;
import org.springframework.http.ResponseEntity;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserRepositoryImpl userRepositoryImpl;

    @Mock
    private passwordHash passwordHash;

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
        String username = "test";
        String password = "testOne123**";
        String email = "testing@test.com";

        when(userRepositoryImpl.existsByUsername(username)).thenReturn(true);

        ResponseEntity<?> response = userController.registerUser(username, password, email);

        assertEquals(409, response.getStatusCodeValue());
        assertEquals("Username is already taken!", response.getBody());
    }

    /**
     * Test Case 4: Register a new user with an already existing email - status 409 Conflict
     */
    @Test
    void testRegisterUser_EmailAlreadyExists() throws SQLException {
        String username = "test";
        String password = "testOne123**";
        String email = "testing@test.com";

        when(userRepositoryImpl.existsByUsername(username)).thenReturn(false);
        when(userRepositoryImpl.existsByEmail(email)).thenReturn(true);

        ResponseEntity<?> response = userController.registerUser(username, password, email);

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
}