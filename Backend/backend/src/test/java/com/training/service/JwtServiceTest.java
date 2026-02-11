package com.training.service;

import com.training.configurations.JwtConfig;
import com.training.enums.UserRole;
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
        
        Jwt jwt = jwtService.generateAccessToken(1L, "admin", List.of(101L), List.of(500.0), UserRole.ADMIN);
        
        assertNotNull(jwt);
        assertFalse(jwt.isExpired());
        assertEquals(1L, jwt.getIdFromToken());
        assertEquals(UserRole.ADMIN, jwt.getRole());
    }

    @Test
    void testGenerateRefreshToken() {
        when(jwtConfig.getAccessTokenExpiration()).thenReturn(3600); // Note: JwtService uses getAccessTokenExpiration for refresh token too in current code
        
        Jwt jwt = jwtService.generateRefreshToken(1L, "user", List.of(102L), List.of(100.0), UserRole.USER);
        
        assertNotNull(jwt);
        assertFalse(jwt.isExpired());
        assertEquals(1L, jwt.getIdFromToken());
        assertEquals(UserRole.USER, jwt.getRole());
    }

    @Test
    void testParseToken_ValidToken() {
        when(jwtConfig.getAccessTokenExpiration()).thenReturn(3600);
        Jwt generatedJwt = jwtService.generateAccessToken(1L, "user", List.of(101L), List.of(1000.0), UserRole.USER);
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
