package codeify.controllers;

import codeify.entities.User;
import codeify.persistance.UserRepositoryImpl;
import codeify.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RestController
public class OAuthController {

    @Autowired
    private UserRepositoryImpl userRepository;

    @GetMapping("/oauth2/success")
    public void success(@AuthenticationPrincipal OAuth2User principal, HttpServletResponse response) throws IOException {
        String email = principal.getAttribute("email");
        String name = principal.getAttribute("name");

        try {
            User user = userRepository.oauth2Login(email, name);
            String jwtToken = JwtUtil.generateToken(email);
            log.info("OAuth2 login successful for user: {}", email);

            response.sendRedirect("http://localhost:3000/?token=" + jwtToken + "&name=" + name);
        } catch (Exception e) {
            log.error("OAuth2 login error: {}", e.getMessage());
            response.sendRedirect("http://localhost:3000/login-failed");
        }
    }
}
