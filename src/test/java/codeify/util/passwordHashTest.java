package codeify.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PasswordHashTest {

    @Test
    void testGenerateSalt() throws NoSuchAlgorithmException {
        String salt = passwordHash.generateSalt();
        assertNotNull(salt, "Salt should not be null");
        assertEquals(24, salt.length(), "Salt should be of correct length after Base64 encoding");  // 16 bytes encoded to Base64
    }

    @Test
    void testHashPassword_Success() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String password = "SecurePassword123!";
        String salt = passwordHash.generateSalt();

        String hashedPassword = passwordHash.hashPassword(password, salt);

        assertNotNull(hashedPassword, "Hashed password should not be null");
        assertEquals(88, hashedPassword.length(), "Hashed password should have the correct length after Base64 encoding");  // 64 bytes encoded to Base64
    }

    @Test
    void testHashPassword_InvalidSalt() {
        String password = "SecurePassword123!";
        String invalidSalt = "InvalidBase64";

        assertThrows(IllegalArgumentException.class, () -> {
            passwordHash.hashPassword(password, invalidSalt);
        }, "Should throw IllegalArgumentException for invalid Base64 salt");
    }

    @Test
    void testValidatePassword_Success() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String password = "SecurePassword123!";
        String salt = passwordHash.generateSalt();
        String hashedPassword = passwordHash.hashPassword(password, salt);

        boolean isValid = passwordHash.validatePassword(password, hashedPassword, salt);
        assertTrue(isValid, "Password validation should succeed with correct password and salt");
    }

    @Test
    void testValidatePassword_Failure() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String password = "SecurePassword123!";
        String wrongPassword = "WrongPassword!";
        String salt = passwordHash.generateSalt();
        String hashedPassword = passwordHash.hashPassword(password, salt);

        boolean isValid = passwordHash.validatePassword(wrongPassword, hashedPassword, salt);
        assertFalse(isValid, "Password validation should fail with incorrect password");
    }

    @Test
    void testValidatePassword_ExceptionHandling() {
        try (MockedStatic<passwordHash> mockedHash = mockStatic(passwordHash.class)) {
            mockedHash.when(() -> passwordHash.hashPassword(anyString(), anyString()))
                    .thenThrow(new NoSuchAlgorithmException("Mocked exception"));

            boolean isValid = passwordHash.validatePassword("password", "hashed", "salt");
            assertFalse(isValid, "Should return false when hashing fails");
        }
    }
}