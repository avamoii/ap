package org.example.middleware;

import io.jsonwebtoken.Claims;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.core.RequestContext;
import org.example.exception.UnauthorizedException;
import org.example.util.JwtUtil;
import java.util.Set;

public class AuthMiddleware implements Middleware {
    private final Set<String> publicPaths = Set.of("/auth/register", "/auth/login");

    @Override
    public boolean handle(HttpRequest request, HttpResponse response) throws Exception {
        String path = request.getPath();

        // Skip authentication for public paths
        if (publicPaths.contains(path)) {
            return true;
        }

        // Check if path requires authentication
        if (requiresAuth(path)) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new UnauthorizedException("Unauthorized: Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);
            Claims claims = JwtUtil.verifyToken(token);

            // Store user info in request context
            RequestContext.current().setAttribute("userId", Long.parseLong(claims.getSubject()));
            RequestContext.current().setAttribute("userRole", claims.get("role", String.class));
        }

        return true;
    }

    private boolean requiresAuth(String path) {
        return path.startsWith("/auth/") ||
                path.startsWith("/restaurants") ||
                path.startsWith("/vendors") ||
                path.startsWith("/items") ||
                path.startsWith("/coupons") ||
                path.startsWith("/orders") ||
                path.startsWith("/favorites") ||
                path.startsWith("/ratings") ||
                path.startsWith("/deliveries") ||
                path.startsWith("/transactions") ||
                path.startsWith("/wallet") ||
                path.startsWith("/payment") ||
                path.startsWith("/admin");
    }
}