package codeify.service.implementations;

import codeify.persistance.interfaces.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            // Find user in your database
            codeify.entities.User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Handle both OAuth and regular users
            String password = user.getPassword() != null ? user.getPassword() : "";
            String authority = "ROLE_" + user.getRole().name();

            return User.builder()
                    .username(user.getUsername())
                    .password(password)
                    .authorities(authority)
                    .build();

        } catch (SQLException e) {
            throw new UsernameNotFoundException("Database error", e);
        }
    }
}