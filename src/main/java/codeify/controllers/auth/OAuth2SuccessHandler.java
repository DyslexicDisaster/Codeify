package codeify.controllers.auth;

import codeify.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2SuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest req,
            HttpServletResponse res,
            Authentication auth) throws IOException {

        String email = ((OAuth2User) auth.getPrincipal()).getAttribute("email");
        logger.debug("[OAuth2] authentication success for email={}", email);

        String token = JwtUtil.generateToken(email);
        logger.debug("[OAuth2] generated JWT: {}", token);

        String targetUrl = UriComponentsBuilder
                .fromUriString("http://localhost:3000/oauth2/redirect")
                .queryParam("token", token)
                .queryParam("username", email)
                .build()
                .toUriString();

        logger.debug("[OAuth2] redirecting to front-end at {}", targetUrl);
        getRedirectStrategy().sendRedirect(req, res, targetUrl);
    }
}