package codeify.controllers.auth;

import codeify.entities.User;
import codeify.entities.role;
import codeify.persistance.implementations.UserRepositoryImpl;
import lombok.extern.slf4j.Slf4j;
//import javax.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponse;

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

@Slf4j
@RestController
@RequestMapping("/api/auth/")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthenticationController {

    @Autowired
    private UserRepositoryImpl userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletResponse response) {  // Note: using jakarta.servlet.http.HttpServletResponse
        log.info("Received login request for user: {}", username);
        try {
            String token = userRepository.login(username, password);
            if (token != null) {
                log.info("User {} logged in successfully.", username);
                Map<String, String> responseBody = new HashMap<>();
                responseBody.put("message", "Login successful");
                responseBody.put("token", token);
                responseBody.put("username", username);
                responseBody.put("role", userRepository.findByUsername(username).get().getRole().toString());

                // Set JWT as HttpOnly cookie using Lax for local testing
                response.addHeader("Set-Cookie", "jwtToken=" + token + "; Path=/; SameSite=Lax");

                return ResponseEntity.ok(responseBody);
            }
            log.warn("Login failed for user: {} - Invalid username or password", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password");
        } catch (SQLException e) {
            log.error("Login failed for user: {} - SQL Error: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Login failed: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestParam String username, @RequestParam String password, @RequestParam String email) {
        log.info("Received registration request for username: {}", username);
        try {
            if (userRepository.existsByUsername(username)) {
                log.warn("Registration failed: Username {} already exists", username);
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
            }
            if (userRepository.existsByEmail(email)) {
                log.warn("Registration failed: Email {} already exists", email);
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
            }

            LocalDate registrationDate = LocalDate.now();
            role defaultRole = role.user;

            User newUser = new User(username, password, email, registrationDate, defaultRole);
            boolean registered = userRepository.register(newUser);

            if (registered) {
                log.info("User {} registered successfully", username);
                return ResponseEntity.ok("User registered successfully");
            } else {
                log.error("Registration failed for user: {}", username);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed");
            }
        } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Error during registration for user {}: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during registration for user {}: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
        }
    }
}