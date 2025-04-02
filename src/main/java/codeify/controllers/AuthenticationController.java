package codeify.controllers;

import codeify.entities.User;
import codeify.persistance.UserRepositoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth/")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthenticationController {

    @Autowired
    private UserRepositoryImpl userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestParam String username, @RequestParam String password) {
        try {
            String token = userRepository.login(username, password);
            if (token != null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Login successful");
                response.put("token", token);
                response.put("username", username);
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login failed: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestParam String username, @RequestParam String password, @RequestParam String email) {
        try {
            if (userRepository.existsByUsername(username)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
            }
            if (userRepository.existsByEmail(email)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
            }

            User newUser = new User(username, password, email, null);
            boolean registered = userRepository.register(newUser);

            if (registered) {
                return ResponseEntity.ok("User registered successfully");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed");
            }
        } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}