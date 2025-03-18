package codeify.controllers;

import codeify.util.passwordHash;
import codeify.entities.User;
import codeify.persistance.UserRepositoryImpl;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user/")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    private UserRepositoryImpl userRepositoryImpl;

    /**
     * Registers a new user in the database.
     *
     * @param username String username to be registered
     * @param password String password to be hashed
     * @param email String email to be registered
     * @return ResponseEntity with a message and the username if successful
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @RequestParam(name="username") String username,
            @RequestParam(name="password") String password,
            @RequestParam(name="email") String email) {

        // Checks if all fields are filled
        if (username.isBlank() || password.isBlank() || email.isBlank()){
            return ResponseEntity.badRequest().body("All fields must be filled out");
        }

        try {
            // Check if username or email already exists
            if (userRepositoryImpl.existsByUsername(username)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Username is already taken!");
            }
            if (userRepositoryImpl.existsByEmail(email)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Email is already taken!");
            }

            // Hash the password and create a new user
            String salt = passwordHash.generateSalt();
            String hashedPassword = passwordHash.hashPassword(password, salt);
            LocalDate registerDate = LocalDate.now();
            User newUser = new User(username, hashedPassword, salt, email, registerDate);

            // Register the new user in the database
            boolean added = userRepositoryImpl.register(newUser);
            if (added) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "User registered successfully");
                response.put("username", username);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Registration has failed.");
            }
        } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred during registration - " + e.getMessage());
        }
    }

    /**
     * Logs in a user and generates a JWT token.
     *
     * @param username String username to be logged in
     * @param password String password to be checked
     * @return ResponseEntity with a message and the JWT token if successful
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(
            @RequestParam(name="username") String username,
            @RequestParam(name="password") String password) {

        // Check that all fields are filled
        if (username.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest().body("All fields must be filled out");
        }

        try {
            // Validate credentials and generate JWT token on success.
            String token = userRepositoryImpl.login(username, password);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid username/password combination");
            }
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("username", username);
            return ResponseEntity.ok(response);

        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred during login - " + e.getMessage());
        }
    }

    /**
     * Logs out a user.
     *
     * @return ResponseEntity with a message
     */
    @GetMapping("/logout")
    public ResponseEntity<?> logout() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout successful. Please remove your token on the client.");
        return ResponseEntity.ok(response);
    }
}
