package codeify.controllers;

import codeify.business.User;
import codeify.persistance.UserDaoImpl;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import codeify.persistance.UserDao;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.time.LocalDate;

@Slf4j
@Controller
public class UserController {

    /**
     * Handles user registration.
     *
     * @param username   the username entered by the user.
     * @param password   the password entered by the user.
     * @param email      the email address entered by the user.
     * @param model      the Model object used to pass data to the view.
     * @return the name of the success or error view based on the operation result.
     */
    @PostMapping("/register")
    public String registerUser(
            @RequestParam(name="username") String username,
            @RequestParam(name="password") String password,
            @RequestParam(name="email") String email,
            Model model, RedirectAttributes redirectAttributes) {

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
            } else {
                String message = "Registration failed for username: " + username;
                model.addAttribute("message", message);
                log.warn("Registration failed for username {}", username);
            }
        } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Error during registration for username {}: {}", username, e.getMessage());
            model.addAttribute("errMsg", "An error occurred during registration");
            return "error";
        }
        return "redirect:/login";
    }

    /**
     * Handles user login.
     *
     * @param username the username entered by the user.
     * @param password the password entered by the user.
     * @param model    the Model object used to pass data to the view.
     * @param session  the HttpSession to store the logged-in user's information.
     * @return the home page on successful login or an error view if login fails.
     */
    @PostMapping("/login")
    public String loginUser(
            @RequestParam(name="username") String username,
            @RequestParam(name="password") String password,
            Model model, HttpSession session) {

        if (username.isBlank() || password.isBlank()) {
            model.addAttribute("errMsg", "All fields must be filled out");
            return "error";
        }

        UserDao userDao = new UserDaoImpl("database.properties");

        try {
            User user = userDao.login(username, password);
            if (user == null) {
                String message = "Invalid username/password combination";
                model.addAttribute("message", message);
                log.warn("Failed login attempt for username {}", username);
                return "loginFailed";
            }

            session.setAttribute("loggedInUser", user);
            log.info("User '{}' logged in successfully", username);
            return "redirect:/";

        } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Error during login for username {}: {}", username, e.getMessage());
            model.addAttribute("errMsg", "An error occurred during login");
            return "error";
        }
    }

    /**
     * Logs out the currently logged-in user.
     *
     * @param session the HttpSession to invalidate the user's session.
     * @return redirects to the home page after logout.
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user != null) {
            log.info("User {} logged out", user.getUsername());
        }
        session.invalidate();
        return "redirect:/";
    }
}
