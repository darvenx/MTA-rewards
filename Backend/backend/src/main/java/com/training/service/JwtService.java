package com.training.service;

import com.training.configurations.JwtConfig;
import com.training.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class JwtService {
    private final JwtConfig jwtConfig;

    public JwtService(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    public Jwt generateAccessToken(Long id, String userName, List<Long> ids, UserRole role){
        return generateToken(id,userName,ids,jwtConfig.getAccessTokenExpiration(),role);
    }

    public Jwt generateRefreshToken(Long id, String userName, List<Long> ids ,UserRole role){
        return generateToken(id,userName,ids,jwtConfig.getAccessTokenExpiration(),role);
    }

    private Jwt generateToken(Long id, String username,List<Long> accounts,
                              int expiration, UserRole role){
        Claims claims = Jwts.claims()
                .subject(id.toString())
                .add("username",username)
                .add("accounts",accounts)
                .add("role",role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + (1000L * expiration)))
                .build();

        return new Jwt(claims, Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes()));
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Jwt parseToken(String token){
        try{
            Claims claims = getClaims(token);
            return new Jwt(claims,Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes()));
        }
        catch (JwtException je){
            return null;
        }

    }
}
