package codeify.service.implementations;

import codeify.entities.User;
import codeify.entities.role;
import codeify.persistance.interfaces.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CustomOAuth2UserService
        implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final Logger logger =
            LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final UserRepository userRepo;

    public CustomOAuth2UserService(
            @Qualifier("userRepositoryImpl") UserRepository userRepo
    ) {
        this.userRepo = userRepo;
    }

    /**
     * Loads the user from the OAuth2 provider and processes it.
     *
     * @param req the OAuth2 user request
     * @return the processed OAuth2 user
     * @throws OAuth2AuthenticationException if an error occurs during processing
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest req)
            throws OAuth2AuthenticationException {
        OAuth2User oauthUser = delegate.loadUser(req);
        try {
            return process(req, oauthUser);
        } catch (SQLException ex) {
            logger.error("[OAuth2] DB error persisting user", ex);
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("database_error"),
                    "Failed to persist OAuth user", ex
            );
        }
    }

    /**
     * Processes the OAuth2 user by checking if it exists in the database and
     * creating a new user if it doesn't.
     *
     * @param req      the OAuth2 user request
     * @param oauthUser the OAuth2 user
     * @return the processed OAuth2 user
     * @throws SQLException if an error occurs during processing
     */
    private OAuth2User process(OAuth2UserRequest req, OAuth2User oauthUser)
            throws SQLException {
        String registrationId = req.getClientRegistration().getRegistrationId();
        String email = oauthUser.getAttribute("email");
        logger.debug("[OAuth2] processing {} user, email={}", registrationId, email);

        if (email == null) {
            logger.error("[OAuth2] email not provided by {}", registrationId);
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("email_not_found"),
                    "Email not returned from provider"
            );
        }

        Optional<User> existing = userRepo.findByEmail(email);
        User user;
        if (existing.isPresent()) {
            user = existing.get();
            logger.debug("[OAuth2] existing user found: username={} email={}",
                    user.getUsername(), email);

        } else {
            user = new User();
            user.setEmail(email);
            user.setUsername(oauthUser.getAttribute("name"));
            user.setProvider(registrationId);
            user.setRole(role.user);
            user.setRegistrationDate(LocalDate.now());
            user.setPassword("");

            logger.debug("[OAuth2] creating new user: {}", user.getUsername());
            userRepo.save(user);
        }
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                oauthUser.getAttributes(),
                "email"
        );
    }
}