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
    private static final String HARDCODED_API_KEY = "Zx9ENYpcTfAruhX9U4lfoqZynG8SsV2KiER11rM487qN0qVjrJZaq59ktTuUfqITteMM8v5dVB5hd7qWAme7EQWFZbK4FIuBgMx6Wuh7PqoxUmsIqOR1eS0KsJU3Vqiw";

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
