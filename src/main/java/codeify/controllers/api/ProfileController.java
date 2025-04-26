package codeify.controllers.api;

import codeify.entities.User;
import codeify.persistance.implementations.UserRepositoryImpl;
import codeify.persistance.implementations.UserProgressRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins="http://localhost:3000", allowCredentials="true")
public class ProfileController {

    @Autowired private UserRepositoryImpl userRepo;
    @Autowired private UserProgressRepositoryImpl progressRepo;

    @GetMapping
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetails principal) {
        try {
            String username = principal.getUsername();
            User user = userRepo.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            int totalScore = progressRepo.getTotalScore(user.getUserId());

            Map<String,Object> body = new HashMap<>();
            body.put("username", user.getUsername());
            body.put("email", user.getEmail());
            body.put("registrationDate", user.getRegistrationDate());
            body.put("role", user.getRole());
            body.put("totalScore", totalScore);

            return ResponseEntity.ok(body);
        } catch (SQLException e) {
            return ResponseEntity.status(500).body("Error fetching profile: " + e.getMessage());
        }
    }
}
