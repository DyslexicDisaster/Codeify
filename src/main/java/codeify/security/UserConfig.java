package codeify.security;

import codeify.entities.User;
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
                // First try to find by username
                return userRepository.findByUsername(principal)
                        // If not found, try email
                        .or(() -> {
                            try {
                                return userRepository.findByEmail(principal);
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        // Throw exception if not found
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + principal));
            } catch (SQLException e) {
                throw new RuntimeException("Database error fetching user", e);
            }
        };
    }
}