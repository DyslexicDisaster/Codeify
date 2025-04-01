package codeify.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    private static final String USERNAME = "testUser";

    /**
     * Test that a token can be generated.
     */
    @Test
    public void testGenerateToken() {
        String token = JwtUtil.generateToken(USERNAME);
        assertNotNull(token);
    }

    /**
     * Test that a token is valid for the given username.
     */
    @Test
    public void testValidateToken() {
        String token = JwtUtil.generateToken(USERNAME);
        assertTrue(JwtUtil.validateToken(token, USERNAME));
    }

    /**
     * Test that the username extracted from a token matches the original username.
     */
    @Test
    public void testGetUsernameFromToken() {
        String token = JwtUtil.generateToken(USERNAME);
        String username = JwtUtil.getUsernameFromToken(token);
        assertEquals(USERNAME, username);
    }

    /**
     * Test with a freshly generated token.
     */
    @Test
    public void testIsTokenExpired() {
        String token = JwtUtil.generateToken(USERNAME);
        assertFalse(JwtUtil.isTokenExpired(token));
    }

    /**
     * Test with a token that expired 5 seconds ago.
     */
    @Test
    public void testExpiredToken() {
        Key key = JwtUtil.getSigningKey();
        String expiredToken = Jwts.builder()
                .setSubject(USERNAME)
                .setIssuedAt(new Date(System.currentTimeMillis() - 10000))
                .setExpiration(new Date(System.currentTimeMillis() - 5000))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        assertTrue(JwtUtil.isTokenExpired(expiredToken));
        assertFalse(JwtUtil.validateToken(expiredToken, USERNAME));
    }

    /**
     * Test with an invalid token.
     */
    @Test
    public void testInvalidToken() {
        String invalidToken = "this.is.an.invalid.token";

        assertThrows(Exception.class, () -> JwtUtil.getUsernameFromToken(invalidToken));

        assertFalse(JwtUtil.validateToken(invalidToken, USERNAME));
    }

    /**
     * Test with a null token.
     */
    @Test
    public void testNullToken() {
        String nullToken = null;

        assertThrows(IllegalArgumentException.class, () -> JwtUtil.getUsernameFromToken(nullToken));

        assertThrows(IllegalArgumentException.class, () -> JwtUtil.isTokenExpired(nullToken));

        assertFalse(JwtUtil.validateToken(nullToken, USERNAME));
    }

    /**
     * Test with an empty token.
     */
    @Test
    public void testEmptyToken() {
        String emptyToken = "";

        assertThrows(IllegalArgumentException.class, () -> JwtUtil.getUsernameFromToken(emptyToken));

        assertThrows(IllegalArgumentException.class, () -> JwtUtil.isTokenExpired(emptyToken));

        assertFalse(JwtUtil.validateToken(emptyToken, USERNAME));
    }
}