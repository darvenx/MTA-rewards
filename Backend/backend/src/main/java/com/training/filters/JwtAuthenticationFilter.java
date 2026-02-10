package com.training.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.enums.UserRole;
import com.training.service.JwtService;
import com.training.service.Jwt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String auth = request.getHeader("Authorization");

        if (null == auth || !auth.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = auth.replace("Bearer ", "");
        Jwt jwt = jwtService.parseToken(token);
        try {
            if (null == jwt || jwt.isExpired()) {
                filterChain.doFilter(request, response);
                return;
            }

            UserRole role = jwt.getRole();
            Long id = jwt.getIdFromToken();

            var authentication = new UsernamePasswordAuthenticationToken(
                    id,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );


            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);

        }
        catch (Exception be) {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            ObjectMapper mapper = new ObjectMapper();
            String errorJson = mapper.writeValueAsString(Map.of("error", be.getMessage()));

            // Write error response only once
            response.getWriter().write(errorJson);
            response.getWriter().flush();
        }
    }
}