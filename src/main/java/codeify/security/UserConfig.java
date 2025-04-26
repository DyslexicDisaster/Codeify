package codeify.security;

import codeify.persistance.interfaces.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.sql.SQLException;

@Configuration
public class UserConfig {

    private final UserRepository userRepository;

    public UserConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return principal -> {
            try {
                // lookup by username, then by email
                return userRepository.findByUsername(principal)
                        .or(() -> {
                            try {
                                return userRepository.findByEmail(principal);
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .map(u -> org.springframework.security.core.userdetails.User
                                .withUsername(u.getUsername())
                                .password(u.getPassword() != null ? u.getPassword() : "")
                                .authorities("ROLE_" + u.getRole().name())
                                .build()
                        )
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + principal));
            } catch (SQLException e) {
                throw new RuntimeException("Database error occurred while fetching user details", e);
            }
        };
    }
}