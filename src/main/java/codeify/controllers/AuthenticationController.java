package codeify.controllers;

import codeify.auth.JwtUtil;
import codeify.dtos.LoginResponse;
import codeify.dtos.LoginUserDto;
import codeify.dtos.RegisterUserDto;
import codeify.model.User;
import codeify.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.Map;

@RestController
public class AuthenticationController {

    private final JwtUtil jwtUtil;
    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtUtil jwtUtil, AuthenticationService authenticationService) {
        this.jwtUtil = jwtUtil;
        this.authenticationService = authenticationService;
    }

    // Register a new user
    @PostMapping("/auth/signup")
    public ResponseEntity<User> register(@RequestBody RegisterUserDto registerUserDto) throws SQLException {
        User registeredUser = authenticationService.register(registerUserDto);
        return ResponseEntity.ok(registeredUser);
    }

    // Authenticate a user
    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto) throws SQLException {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);
        String jwtToken = jwtUtil.generateToken(Map.of(), authenticatedUser);

        LoginResponse response = new LoginResponse(jwtToken, jwtUtil.getExpirationTime());

        return ResponseEntity.ok(response);
    }
}