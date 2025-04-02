package codeify.security;

import codeify.util.passwordHash;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class JwtPasswordEncoder implements PasswordEncoder {

    // The password hash generator (hash + salt)
    @Override
    public String encode(CharSequence rawPassword) {
        try {
            // Generate a random salt
            String salt = passwordHash.generateSalt();

            // Hash the password with the salt
            String hash = passwordHash.hashPassword(rawPassword.toString(), salt);
            return salt + ":" + hash;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error encoding password", e);
        }
    }

    // The password hash validator
    @Override
    public boolean matches(CharSequence rawPassword, String storedPassword) {
        try {
            // Split the stored password into salt and hash
            String[] parts = storedPassword.split(":");
            if (parts.length != 2) return false;
            String salt = parts[0];
            String hash = parts[1];

            // Validate the password, if valid returns true
            return passwordHash.validatePassword(rawPassword.toString(), hash, salt);
        } catch (Exception e) {
            return false;
        }
    }
}