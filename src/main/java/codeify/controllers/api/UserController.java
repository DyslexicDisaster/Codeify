package codeify.controllers.api;

import codeify.entities.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user/")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class UserController {

    /*
     * This method handles user logout. It invalidates the session and clears the user data.
     */
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
