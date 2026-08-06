package com.kalivira.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    //read secret
    @Value("${jwt.secret}")
    private String secret;

    //read expiration
    @Value("${jwt.expiration}")
    private long expiration;


    //KEY GENERATE
    private Key getSigningKey(){
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    //JWT generate
    public String generateToken(String email){

        return Jwts.builder().setSubject(email).
                setIssuedAt(new Date()).
                setExpiration(new Date(System.currentTimeMillis()+expiration)).
                signWith(getSigningKey(),SignatureAlgorithm.HS256).
                compact();
    }


    //extract email
    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }


    //validate token
    public boolean validateToken(String token, String email) {
        String extractedEmail = extractEmail(token);
        return extractedEmail.equals(email);
    }
}
