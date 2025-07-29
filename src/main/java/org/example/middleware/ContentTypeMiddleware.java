package org.example.middleware;

import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.exception.UnsupportedMediaTypeException;

public class ContentTypeMiddleware implements Middleware {
    @Override
    public boolean handle(HttpRequest request, HttpResponse response) throws Exception {
        String method = request.getMethod();

        if (("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method))) {
            String contentType = request.getHeader("Content-Type");
            String body = request.getBody();

            if (body != null && !body.isEmpty()) {
                if (contentType == null || !contentType.toLowerCase().contains("application/json")) {
                    throw new UnsupportedMediaTypeException("Unsupported Media Type: Only application/json is supported.");
                }
            }
        }

        return true; // Continue to next middleware
    }
}