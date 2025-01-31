package codeify.config;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class passwordHash {
    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 64;
    private static final int ITERATIONS = 10000;

    /**
     * Generates a random salt for password hashing.
     *
     * @return Base64 encoded salt string
     * @throws NoSuchAlgorithmException If the hashing algorithm isn't available
     * @author Malikom12
     */
    public static String generateSalt() throws NoSuchAlgorithmException {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Creates a hash of the password using the provided salt.
     * Uses PBKDF2 with SHA-256 for secure password hashing.
     *
     * @param password The password to hash
     * @param salt The salt to use in hashing
     * @return Base64 encoded hash of the password
     * @throws NoSuchAlgorithmException If the hashing algorithm isn't available
     * @throws InvalidKeySpecException If there's an error with the key specification
     * @author Malikom12
     */
    public static String hashPassword(String password, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] saltBytes = Base64.getDecoder().decode(salt);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, ITERATIONS, HASH_LENGTH * 8);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hashBytes = factory.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hashBytes);
    }
}