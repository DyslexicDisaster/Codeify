package codeify.controllers.auth;

import codeify.dtos.RegisterRequest;
import codeify.dtos.UserDto;
import codeify.entities.User;
import codeify.entities.role;
import codeify.persistance.implementations.UserRepositoryImpl;
import codeify.service.implementations.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * RESOURCES:
 * https://www.baeldung.com/spring-boot-bean-validation
 * https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-validation
 * https://docs.spring.io/spring-framework/reference/core/validation/beanvalidation.html#validation-beanvalidation-spring
 * https://www.geeksforgeeks.org/hibernate-validator-with-example/
 */

@Slf4j
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthenticationController {

    @Autowired
    private UserRepositoryImpl userRepository;
    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletResponse response) {
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
                response.addHeader("Set-Cookie", "jwtToken=" + token + "; Path=/;SameSite=Lax");

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

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) throws SQLException {
        String principal = authentication.getName();
        log.debug("[AuthController] /me called, principal = {}", principal);
        Optional<User> userOpt = userRepository.findByUsername(principal);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(principal);
        }

        User user = userOpt
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + principal));
        UserDto dto = new UserDto(user.getUsername(), user.getRole().name());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody RegisterRequest req,
            BindingResult br
    ) {
        if (br.hasErrors()) {
            Map<String,String> errs = br.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            fe -> fe.getField(),
                            fe -> fe.getDefaultMessage()
                    ));
            return ResponseEntity.badRequest().body(errs);
        }

        try {
            if (userRepository.existsByUsername(req.getUsername())) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(Map.of("username", "Username already exists"));
            }
            if (userRepository.existsByEmail(req.getEmail())) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(Map.of("email", "Email already exists"));
            }

            User u = new User(
                    req.getUsername(),
                    req.getPassword(),
                    req.getEmail(),
                    LocalDate.now(),
                    role.user,
                    "local"
            );
            boolean ok = userRepository.register(u);
            if (!ok) {
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Registration failed");
            }
            return ResponseEntity.ok("User registered successfully");

        } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Registration error for {}: {}", req.getUsername(), e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @RequestParam("email") String email,
            HttpServletRequest request
    ) {
        log.info("Received password reset request for email: {}", email);

        String appUrl = request.getScheme() + "://" +
                request.getServerName() + ":" +
                request.getServerPort();

        try {
            passwordResetService.createPasswordResetToken(email, appUrl);
            return ResponseEntity.ok("Password reset link sent to " + email);
        } catch (Exception e) {
            log.error("Error sending password reset link to {}: {}", email, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        log.info("Received password reset request with token: {}", token);
        try {
            passwordResetService.resetPassword(token, newPassword);
            return ResponseEntity.ok("Password reset successfully");
        } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Error resetting password with token {}: {}", token, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid password reset request with token {}: {}", token, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}