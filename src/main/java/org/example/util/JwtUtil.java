// File: src/main/java/org/example/util/JwtUtil.java
package org.example.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.example.exception.UnauthorizedException; // <-- ایمپورت کردن Exception سفارشی

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

public class JwtUtil {
    // ... بقیه متغیرها ...

    public static String generateToken(Long userId, String role) {
        // ... این متد بدون تغییر باقی می‌ماند ...
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 864_000_00);

        return Jwts.builder()
                .setIssuer("your-app-name")
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor("YourVeryLongAndHardToGuessSecretKeyForHS256!@#$".getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    public static Claims verifyToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor("YourVeryLongAndHardToGuessSecretKeyForHS256!@#$".getBytes(StandardCharsets.UTF_8)))
                    .requireIssuer("your-app-name")
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // به جای RuntimeException، خطای مشخص خودمان را پرتاب می‌کنیم
            throw new UnauthorizedException("Token has expired.");
        } catch (JwtException e) { // یک catch عمومی برای تمام خطاهای دیگر JWT
            // برای تمام خطاهای دیگر JWT نیز، خطای Unauthorized پرتاب می‌کنیم
            throw new UnauthorizedException("Invalid or corrupted token: " + e.getMessage());
        }
    }

    // ...
}