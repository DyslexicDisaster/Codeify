package codeify.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

public class ApiKeyAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyAuthenticationFilter.class);

    private final String headerName;

    public ApiKeyAuthenticationFilter(final String headerName) {
        this.headerName = headerName;
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        String apiKey = request.getHeader(headerName);
        LOGGER.debug("Extracted API Key: {}", apiKey);
        return apiKey;
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return null;
    }
}