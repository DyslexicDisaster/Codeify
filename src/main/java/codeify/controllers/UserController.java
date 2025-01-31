package codeify.controllers;

import codeify.business.User;
import codeify.persistance.UserDaoImpl;
import org.springframework.ui.Model;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import codeify.persistance.UserDao;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.time.LocalDate;

@Slf4j
@Controller
public class UserController {
    @PostMapping("/register")
    public String registerUser(
            @RequestParam(name="username") String username,
            @RequestParam(name="password") String password,
            @RequestParam(name="email") String email,
            Model model) {

        if (username.isBlank() || password.isBlank() || email.isBlank()) {
            model.addAttribute("errMsg", "All fields must be filled out");
            return "error";
        }

        UserDao userDao = new UserDaoImpl("database.properties");
        LocalDate registrationDate = LocalDate.now();
        User user = new User(username, password, "", email, registrationDate);

        try {
            boolean added = userDao.register(user);
            if (added) {
                model.addAttribute("registeredUser", user);
                log.info("User {} registered successfully", username);
                return "registerSuccess";
            } else {
                String message = "Registration failed for username: " + username;
                model.addAttribute("message", message);
                log.warn("Registration failed for username {}", username);
                return "registerFailed";
            }
        } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Error during registration for username {}: {}", username, e.getMessage());
            model.addAttribute("errMsg", "An error occurred during registration");
            return "error";
        }
    }
}
