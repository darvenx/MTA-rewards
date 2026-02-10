package com.training.service;

import com.training.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.security.authentication.BadCredentialsException;

import javax.crypto.SecretKey;
import java.util.Date;

public class Jwt {
    private final Claims claims;
    private final SecretKey key;

    public Jwt(Claims claims, SecretKey secretKey){
        this.key = secretKey;
        this.claims = claims;

    }

    public boolean isExpired(){
        try{
            return claims.getExpiration().before(new Date());
        }
        catch(JwtException je){
            if(claims.getExpiration().after(new Date()))
                throw new BadCredentialsException("Token expired");
            else
                throw new BadCredentialsException("Invalid Credentials");
        }
    }

    public Long getIdFromToken(){
        return Long.valueOf(claims.getSubject());
    }

    public UserRole getRole(){
        return UserRole.valueOf(claims.get("role",String.class));
    }

    public String toString(){
        return Jwts.builder()
                .claims(claims)
                .signWith(key)
                .compact();
    }
}