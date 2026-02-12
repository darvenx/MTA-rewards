package com.training.service;

import com.training.configurations.JwtConfig;
import com.training.enums.UserRole;
<<<<<<< HEAD
import com.training.jwt.Jwt;
import com.training.jwt.JwtService;
=======
>>>>>>> c2bcfbfa6018b37f88a52eef71d69fefd0f1cf24
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    @Mock
    private JwtConfig jwtConfig;

    @InjectMocks
    private JwtService jwtService;

    private String secret = "mySecretKeyMySecretKeyMySecretKeyMySecretKey"; // Must be long enough for HmacSHA256

    @BeforeEach
    void setUp() {
        when(jwtConfig.getSecret()).thenReturn(secret);
    }

    @Test
    void testGenerateAccessToken() {
        when(jwtConfig.getAccessTokenExpiration()).thenReturn(3600);
        
<<<<<<< HEAD
        Jwt jwt = jwtService.generateAccessToken(1L, "admin", List.of(101L), UserRole.ADMIN);
=======
        Jwt jwt = jwtService.generateAccessToken(1L, "admin", List.of(101L), List.of(500.0), UserRole.ADMIN);
>>>>>>> c2bcfbfa6018b37f88a52eef71d69fefd0f1cf24
        
        assertNotNull(jwt);
        assertFalse(jwt.isExpired());
        assertEquals(1L, jwt.getIdFromToken());
<<<<<<< HEAD
        
        // Verify role by parsing the token since direct access on fresh object fails with current implementation
        Jwt parsedJwt = jwtService.parseToken(jwt.toString());
        assertEquals(UserRole.ADMIN, parsedJwt.getRole());
=======
        assertEquals(UserRole.ADMIN, jwt.getRole());
>>>>>>> c2bcfbfa6018b37f88a52eef71d69fefd0f1cf24
    }

    @Test
    void testGenerateRefreshToken() {
        when(jwtConfig.getAccessTokenExpiration()).thenReturn(3600); // Note: JwtService uses getAccessTokenExpiration for refresh token too in current code
        
<<<<<<< HEAD
        Jwt jwt = jwtService.generateRefreshToken(1L, "user", List.of(102L), UserRole.USER);
=======
        Jwt jwt = jwtService.generateRefreshToken(1L, "user", List.of(102L), List.of(100.0), UserRole.USER);
>>>>>>> c2bcfbfa6018b37f88a52eef71d69fefd0f1cf24
        
        assertNotNull(jwt);
        assertFalse(jwt.isExpired());
        assertEquals(1L, jwt.getIdFromToken());
<<<<<<< HEAD
        
        // Verify role by parsing the token
        Jwt parsedJwt = jwtService.parseToken(jwt.toString());
        assertEquals(UserRole.USER, parsedJwt.getRole());
=======
        assertEquals(UserRole.USER, jwt.getRole());
>>>>>>> c2bcfbfa6018b37f88a52eef71d69fefd0f1cf24
    }

    @Test
    void testParseToken_ValidToken() {
        when(jwtConfig.getAccessTokenExpiration()).thenReturn(3600);
<<<<<<< HEAD
        Jwt generatedJwt = jwtService.generateAccessToken(1L, "user", List.of(101L), UserRole.USER);
=======
        Jwt generatedJwt = jwtService.generateAccessToken(1L, "user", List.of(101L), List.of(1000.0), UserRole.USER);
>>>>>>> c2bcfbfa6018b37f88a52eef71d69fefd0f1cf24
        String tokenString = generatedJwt.toString();

        Jwt parsedJwt = jwtService.parseToken(tokenString);
        
        assertNotNull(parsedJwt);
        assertEquals(1L, parsedJwt.getIdFromToken());
        assertEquals(UserRole.USER, parsedJwt.getRole());
    }

    @Test
    void testParseToken_InvalidToken() {
        Jwt parsedJwt = jwtService.parseToken("invalid.token.string");
        assertNull(parsedJwt);
    }
}
