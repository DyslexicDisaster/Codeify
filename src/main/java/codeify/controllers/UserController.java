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

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestParam(name="username") String username,
                                          @RequestParam(name="password") String password,
                                          @RequestParam(name="email") String email){

        // Checks if all fields are filled
        if (username.isBlank() || password.isBlank() || email.isBlank()){
            return ResponseEntity.badRequest().body("All fields must be filled out");
        }

        try{
            // If the username is already inside the database
            if (userRepositoryImpl.existsByUsername(username)){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Username is already taken!");
            }

            // If the email is already inside the database
            if (userRepositoryImpl.existsByEmail(email)){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email is already taken!");
            }

            // Hashes the password
            String salt = passwordHash.generateSalt();
            String hashedPassword = passwordHash.hashPassword(password, salt);

            // Creates user object
            LocalDate registerDate = LocalDate.now();
            User newUser = new User(username, hashedPassword, salt, email, registerDate);

            // Register new user inside the database
            boolean added = userRepositoryImpl.register(newUser);
            if (added){
                Map<String, String> response = new HashMap<>();
                response.put("message", "User registered successfully");
                response.put("username", username);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                // Error message when user has not been added
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration has failed.");
            }
            // Error has occurred inside the database server
        } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error has occured during registration - " + e);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(
            @RequestParam(name="username") String username,
            @RequestParam(name="password") String password,
            HttpSession session) { // Add HttpSession here

        // Checks if all fields are filled
        if (username.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest().body("All fields must be filled out");
        }

        try {
            User user = userRepositoryImpl.login(username, password);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid username/password combination");
            }

            // Store the logged-in user in the session!
            session.setAttribute("loggedInUser", user);

            // Returns a successful login message
            Map<String, String> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("username", username);
            response.put("role", user.getRole().toString());
            return ResponseEntity.ok(response);

        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred during login - " + e);
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        Map<String, String> response = new HashMap<>();

        if (user != null) {
            response.put("message", "Logout successful for user: " + user.getUsername());
        } else {
            response.put("message", "No user is currently logged in");
        }

        session.invalidate();
        return ResponseEntity.ok(response);
    }
}
