package codeify.service.implementations;

import codeify.persistance.interfaces.UserRepository;
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

    /**
     * Loads user details by username or email.
     *
     * @param principal the username or email of the user
     * @return UserDetails object containing user information
     * @throws UsernameNotFoundException if the user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String principal) throws UsernameNotFoundException {
        try {
            codeify.entities.User u = userRepository.findByUsername(principal)
                    .or(() -> {
                        try {
                            return userRepository.findByEmail(principal);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + principal));

            String pwd = u.getPassword() != null ? u.getPassword() : "";
            String role = "ROLE_" + u.getRole().name();

            return org.springframework.security.core.userdetails.User.builder()
                    .username(u.getUsername())
                    .password(pwd)
                    .authorities(role)
                    .build();
        } catch (SQLException e) {
            throw new UsernameNotFoundException("Database error", e);
        }
    }
}