package com.saas.professor.security;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    public String generateAccessToken(Long teacherId, String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("teacherId", teacherId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getKey())
                .compact();
    }

    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException e) {
            System.err.println("JWT invalid: " + e.getMessage());
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAdminToken(Long adminId, String email) {
        return Jwts.builder()
            .setSubject(email)
            .claim("adminId", adminId)
            .claim("role", "ADMIN")
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
            .signWith(getKey())
            .compact();
    }

    public boolean isAdminToken(String token) {
        try {
            Claims claims = getClaims(token);
            return "ADMIN".equals(claims.get("role", String.class));
        } catch (Exception e) {
            return false;
        }
    }
}