package com.training.service;

import com.training.enums.UserRole;
import com.training.jwt.Jwt;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class JwtTest {

    private SecretKey secretKey;
    private String secretString = "mySecretKeyMySecretKeyMySecretKeyMySecretKey"; // Must be long enough for HS256

    @BeforeEach
    void setUp() {
        secretKey = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void testJwtCreationAndGetters() {
        long id = 123L;
        UserRole role = UserRole.USER;
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 10000);

        Claims claims = Jwts.claims()
                .subject(String.valueOf(id))
                .add("role", role.name())
                .expiration(expiration)
                .build();

        Jwt jwt = new Jwt(claims, secretKey);

        assertFalse(jwt.isExpired());
        assertEquals(id, jwt.getIdFromToken());
        assertEquals(role, jwt.getRole());
    }

    @Test
    void testIsExpired_ExpiredToken() {
        Date past = new Date(System.currentTimeMillis() - 10000);

        Claims claims = Jwts.claims()
                .subject("123")
                .expiration(past)
                .build();

        Jwt jwt = new Jwt(claims, secretKey);

        assertTrue(jwt.isExpired());
    }

    @Test
    void testIsExpired_NotExpired() {
        Date future = new Date(System.currentTimeMillis() + 10000);

        Claims claims = Jwts.claims()
                .subject("123")
                .expiration(future)
                .build();

        Jwt jwt = new Jwt(claims, secretKey);

        assertFalse(jwt.isExpired());
    }

    @Test
    void testIsExpired_ThrowsTokenExpired() {
        Claims mockClaims = org.mockito.Mockito.mock(Claims.class);
        Date future = new Date(System.currentTimeMillis() + 10000);

        // Use doThrow/doReturn syntax
        org.mockito.Mockito.doThrow(new JwtException("Mock Exception"))
                .doReturn(future)
                .when(mockClaims).getExpiration();

        Jwt jwt = new Jwt(mockClaims, secretKey);

        assertThrows(BadCredentialsException.class, () -> jwt.isExpired(), "Token expired");
    }

    @Test
    void testIsExpired_ThrowsInvalidCredentials() {
        Claims mockClaims = org.mockito.Mockito.mock(Claims.class);
        Date past = new Date(System.currentTimeMillis() - 10000);

        org.mockito.Mockito.doThrow(new JwtException("Mock Exception"))
                .doReturn(past)
                .when(mockClaims).getExpiration();

        Jwt jwt = new Jwt(mockClaims, secretKey);

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> jwt.isExpired());
        assertEquals("Invalid Credentials", exception.getMessage());
    }

    // Testing the specific exception handling in Jwt.isExpired()
    // The code catches JwtException. If expiration is after now, throws "Token
    // expired" (BadCredentialsException)
    // If expiration is before now (or other error), throws "Invalid Credentials"
    // (BadCredentialsException)
    // However, claims.getExpiration().before(new Date()) just returns boolean, it
    // doesn't usually throw JwtException unless claims are invalid/proxy.
    // The try-catch block in Jwt.java seems to be handling potential exceptions
    // from claims.getExpiration() if it was missing or malformed?
    // Actually, Jwts claims implementation is usually a map.

    // Let's create a scenario where we can test the behavior if we can mock Claims
    // or cause an exception.
    // Direct instantiation of Jwt with standard Jwts claims usually works fine.

    @Test
    void testToString() {
        Claims claims = Jwts.claims()
                .subject("123")
                .build();
        Jwt jwt = new Jwt(claims, secretKey);

        String token = jwt.toString();
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }
}
