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
        return username -> {
            try {
                // Retrieves user from the repository
                return userRepository.findByUsername(username)
                        // If user is not found, throws an exception
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
