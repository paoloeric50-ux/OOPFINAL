package com.motorph.payroll.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*
 * JwtUtil.java - Utility class for generating and reading JWT tokens
 *
 * JWT (JSON Web Token) is how the app keeps users "logged in" without
 * using traditional server sessions. When you log in, you get a token.
 * Every time you make a request, you send that token so the server knows
 * who you are.
 *
 * A JWT has 3 parts separated by dots:
 *   header.payload.signature
 *
 * The signature is made using a secret key, so nobody can fake a token.
 *
 * Author: Group 5 - MO-IT101
 */
@Component
public class JwtUtil {

    // These values are pulled from application.properties
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-hours}")
    private int jwtExpirationHours;

    /*
     * getSigningKey() - Converts the secret string into a proper HMAC key
     * The key must be at least 256 bits (32 bytes) for SHA-256 signing.
     * If the secret is shorter, we pad it with zero bytes to reach 32 bytes.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // pad the key if it's too short
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 32));
            return Keys.hmacShaKeyFor(paddedKey);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /*
     * generateToken() - Create a new JWT for a logged-in user
     * The token stores the user's ID, email, and role in its payload (claims).
     * The token expires after a set number of hours (24 by default).
     */
    public String generateToken(String userId, String email, String role) {
        // claims are the data we embed inside the token
        Map<String, Object> claims = new HashMap<>();
        claims.put("user_id", userId);
        claims.put("email",   email);
        claims.put("role",    role);

        Date now    = new Date();
        Date expiry = new Date(now.getTime() + (long) jwtExpirationHours * 3600 * 1000);

        // build and sign the token
        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    /*
     * extractAllClaims() - Parse and verify the token, return its payload
     * This also verifies the signature — if the token was tampered with, this throws an exception.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Extract the stored user ID from the token payload
    public String extractUserId(String token) {
        return extractAllClaims(token).get("user_id", String.class);
    }

    // Extract the stored email from the token payload
    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    // Extract the stored role (ADMIN or USER) from the token payload
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // Check if the token's expiry date is in the past
    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    /*
     * validateToken() - Returns true only if the token is:
     * 1. A valid JWT (not tampered with)
     * 2. Not yet expired
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;  // any error means the token is invalid
        }
    }
}
