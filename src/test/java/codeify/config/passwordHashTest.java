package codeify.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.*;

class passwordHashTest {
    private String password;
    private String salt;
    private String hashedPassword;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException, InvalidKeySpecException {
        password = "SecurePassword123";
        salt = passwordHash.generateSalt();
        hashedPassword = passwordHash.hashPassword(password, salt);
    }

    @Test
    void testGenerateSalt() throws NoSuchAlgorithmException {
        String newSalt = passwordHash.generateSalt();
        assertNotNull(newSalt, "Salt should not be null");
        assertNotEquals(salt, newSalt, "New salt should not be the same as the old salt");
    }

    @Test
    void testHashPassword() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String newHashedPassword = passwordHash.hashPassword(password, salt);
        assertNotNull(newHashedPassword, "Hashed password should not be null");
        assertEquals(hashedPassword, newHashedPassword, "New hashed password should not be the same as the old hashed password");
    }

    @Test
    void testValidatePassword() {
        assertTrue(passwordHash.validatePassword(password, hashedPassword, salt), "Password should be valid");
        assertFalse(passwordHash.validatePassword("WrongPassword123", hashedPassword, salt), "Password should not be valid");
    }
}