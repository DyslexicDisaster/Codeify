package codeify.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "security.jwt")
// This class is used to store the JWT configuration properties
public class JwtConfig {
    private String secretKey;
    private long expirationTime;
}
