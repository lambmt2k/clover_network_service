package com.socialmedia.clover_network.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(String userId) {

        // Set the token expiration time (e.g., 1 hour from now)
        long expirationMillis = System.currentTimeMillis() +  90L * 24L * 60L * 60L * 1000L; // 90 days

        // Create the claims for the JWT
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);

        // Build the JWT token
        String encodedString = Base64.getEncoder().encodeToString(secret.getBytes());

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(expirationMillis))
                .signWith(SignatureAlgorithm.HS512, encodedString)
                .compact();

        return token;
    }

}
