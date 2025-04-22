package codeify.service.implementations;

import codeify.entities.ForgottenPasswordToken;
import codeify.entities.User;
import codeify.persistance.interfaces.ForgottenPasswordTokenRepository;
import codeify.persistance.interfaces.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import codeify.util.passwordHash;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.UUID;

/*
 * Resources: https://www.youtube.com/watch?v=mxs_00KpUE4
 */
@Service
@Transactional
public class PasswordResetService {
    private static final long EXPIRATION_TIME = 10;

    @Autowired
    private ForgottenPasswordTokenRepository forgottenPasswordTokenRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.client.url}")
    private String clientUrl;

    @Value("classpath:email.html")
    private Resource emailTemplate;

    /**
     * Creates a password reset token and sends an email to the user.
     *
     * @param email   The email address of the user
     * @param appUrl  The application URL
     * @throws SQLException if a database error occurs
     */
    public void createPasswordResetToken(String email, String appUrl) throws SQLException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user with email " + email));

        forgottenPasswordTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());

        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(EXPIRATION_TIME);
        ForgottenPasswordToken prt = new ForgottenPasswordToken(user, token, expiry);
        forgottenPasswordTokenRepository.save(prt);

        String link = clientUrl + "/reset-password?token=" + token;

        String body = loadTemplate()
                .replace("{{username}}", user.getUsername())
                .replace("{{reset_link}}", link);

        sendHtmlEmail(user.getEmail(), "Password Reset Request", body);
    }

    /**
     * Loads the email template from the classpath.
     *
     * @return The email template as a string
     */
    private String loadTemplate() {
        try (InputStream in = emailTemplate.getInputStream();
             Scanner sc = new Scanner(in, StandardCharsets.UTF_8.name())) {
            sc.useDelimiter("\\A");
            return sc.hasNext() ? sc.next() : "";
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load email template", e);
        }
    }

    /**
     * Sends an HTML email.
     *
     * @param to        The recipient's email address
     * @param subject   The subject of the email
     * @param htmlBody  The HTML body of the email
     */
    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        MimeMessage msg = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(msg);
        } catch (MessagingException e) {
            throw new MailSendException("Failed to send HTML email", e);
        }
    }

    /**
     * Resets the user's password using the provided token and new password.
     *
     * @param token       The password reset token
     * @param newPassword The new password
     * @throws SQLException if a database error occurs
     */
    public void resetPassword(String token, String newPassword)
            throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {

        var fpt = forgottenPasswordTokenRepository.findByToken(token);
        if (fpt == null || fpt.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invalid or expired token");
        }

        String salt = passwordHash.generateSalt();
        String hash = passwordHash.hashPassword(newPassword, salt);
        String combined = salt + ":" + hash;

        userRepository.updatePassword(fpt.getUser().getUserId(), combined);
        forgottenPasswordTokenRepository.delete(fpt);
    }
}
