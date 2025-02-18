package codeify.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class ApiKeyAuthenticationManager implements AuthenticationManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyAuthenticationManager.class);

    // Hardcoded API Key
    private static final String HARDCODED_API_KEY = "my-secret-api-key";

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String providedApiKey = (String) authentication.getPrincipal();

        if (!HARDCODED_API_KEY.equals(providedApiKey)) {
            LOGGER.error("Invalid API Key: {}", providedApiKey);
            throw new BadCredentialsException("Invalid API Key.");
        }

        authentication.setAuthenticated(true);
        return authentication;
    }
}
