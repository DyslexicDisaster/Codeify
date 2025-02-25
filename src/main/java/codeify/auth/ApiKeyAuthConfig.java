package codeify.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Order(1)
public class ApiKeyAuthConfig {

    private static final String API_KEY_AUTH_HEADER_NAME = "x-api-key";

    @Bean
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter(API_KEY_AUTH_HEADER_NAME);
        filter.setAuthenticationManager(new ApiKeyAuthenticationManager());

        http
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilter(filter);

        return http.build();
    }
}
