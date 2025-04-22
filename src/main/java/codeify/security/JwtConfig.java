package codeify.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for JWT properties.
 * This class is used to bind the properties defined in application.properties or application.yml
 * under the prefix "security.jwt" to Java fields.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "security.jwt")
public class JwtConfig {
    private String secretKey;
    private long expirationTime;
}
