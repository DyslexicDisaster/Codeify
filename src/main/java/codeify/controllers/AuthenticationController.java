package codeify.controllers;

import codeify.auth.JwtUtil;
import codeify.dtos.LoginResponse;
import codeify.dtos.LoginUserDto;
import codeify.dtos.RegisterUserDto;
import codeify.entities.User;
import codeify.service.AuthenticationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthenticationController {

    private final JwtUtil jwtUtil;
    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtUtil jwtUtil, AuthenticationService authenticationService) {
        this.jwtUtil = jwtUtil;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterUserDto registerUserDto) {
        try {
            User registeredUser = authenticationService.register(registerUserDto);
            return ResponseEntity.ok(registeredUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody LoginUserDto loginUserDto) {
        try {
            User authenticatedUser = authenticationService.authenticate(loginUserDto);
            String jwtToken = jwtUtil.generateToken(Map.of(), authenticatedUser);
            LoginResponse response = new LoginResponse(jwtToken, jwtUtil.getExpirationTime());
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {

        try {
            Cookie cookie = new Cookie("jwt", null);
            cookie.setMaxAge(0);
            cookie.setHttpOnly(true);
            cookie.setPath("/");

            response.addCookie(cookie);

            return ResponseEntity.ok("Logout successful");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}