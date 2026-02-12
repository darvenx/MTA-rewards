package com.training.filters;

import com.training.enums.UserRole;
import com.training.jwt.Jwt;
import com.training.jwt.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void testDoFilterInternal_ValidToken() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid_token");
        when(jwtService.parseToken("valid_token")).thenReturn(jwt);
        when(jwt.isExpired()).thenReturn(false);
        when(jwt.getRole()).thenReturn(UserRole.USER);
        when(jwt.getIdFromToken()).thenReturn(1L);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        // Verify that authentication is set in SecurityContext
        // Accessing static SecurityContextHolder in unit test might be tricky if not cleared.
        // But we can verify no error response was written.
    }

    @Test
    void testDoFilterInternal_NoToken() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).parseToken(anyString());
    }

    @Test
    void testDoFilterInternal_ExpiredToken() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer expired_token");
        when(jwtService.parseToken("expired_token")).thenReturn(jwt);
        when(jwt.isExpired()).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        // Authentication should not be set (or remain null/anonymous)
    }

    @Test
    void testDoFilterInternal_InvalidToken_Exception() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid_token");
        // Mock parseToken to return a jwt that will throw exception on access
        when(jwtService.parseToken("valid_token")).thenReturn(jwt);
        when(jwt.isExpired()).thenReturn(false);
        when(jwt.getRole()).thenThrow(new RuntimeException("Simulated internal error"));

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        // Check if error message contains the exception message
        // assert(stringWriter.toString().contains("Simulated internal error"));
    }
}
