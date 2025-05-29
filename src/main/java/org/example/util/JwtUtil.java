package org.example.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets; // برای تبدیل رشته به بایت
import java.security.Key;
import java.util.Date;

public class JwtUtil {
    // !! این کلید باید بسیار طولانی و پیچیده باشد و در دنیای واقعی از متغیر محیطی خوانده شود !!
    private static final String SECRET_STRING = "YourVeryLongAndHardToGuessSecretKeyForHS256!@#$";
    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRATION_TIME_MS = 864_000_00; // 1 روز (یا کمتر، مثلا 15 دقیقه: 900_000)
    private static final String ISSUER = "your-app-name"; // نام اپلیکیشن شما

    public static String generateToken(Long userId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME_MS);

        return Jwts.builder()
                .setIssuer(ISSUER) // اضافه کردن Issuer
                .setSubject(String.valueOf(userId)) // موضوع توکن، معمولاً ID کاربر
                .claim("role", role) // اطلاعات اضافی (نقش)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS256) // مشخص کردن الگوریتم امضا
                .compact();
    }

    public static Claims verifyToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SIGNING_KEY)
                    .requireIssuer(ISSUER) // بررسی Issuer هنگام اعتبارسنجی
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            System.err.println("Token has expired: " + e.getMessage());
            throw new RuntimeException("Token has expired."); // یا یک Exception سفارشی
        } catch (UnsupportedJwtException e) {
            System.err.println("Token is unsupported: " + e.getMessage());
            throw new RuntimeException("Token is unsupported.");
        } catch (MalformedJwtException e) {
            System.err.println("Token is malformed: " + e.getMessage());
            throw new RuntimeException("Token is malformed.");
        } catch (SignatureException e) { // این Exception در نسخه‌های قدیمی‌تر jjwt-impl بود، در جدیدترها io.jsonwebtoken.security.SignatureException
            System.err.println("Token signature is invalid: " + e.getMessage());
            throw new RuntimeException("Token signature is invalid.");
        } catch (IllegalArgumentException e) {
            System.err.println("Token claims string is empty: " + e.getMessage());
            throw new RuntimeException("Token claims string is empty.");
        } catch (JwtException e) { // یک Exception عمومی‌تر برای سایر خطاهای JWT
            System.err.println("Invalid or expired token: " + e.getMessage());
            throw new RuntimeException("Invalid or expired token.");
        }
    }

    // متدهای کمکی برای استخراج Claims (اختیاری اما مفید)
    public static Long getUserIdFromClaims(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }

    public static String getUserRoleFromClaims(Claims claims) {
        return claims.get("role", String.class);
    }
}