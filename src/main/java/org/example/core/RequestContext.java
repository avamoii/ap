// 1. Request Context - Holds request-scoped data (like userId, userRole)
package org.example.core;

import java.util.HashMap;
import java.util.Map;

public class RequestContext {
    private static final ThreadLocal<RequestContext> CONTEXT = new ThreadLocal<>();
    private final Map<String, Object> attributes = new HashMap<>();

    public static RequestContext current() {
        RequestContext context = CONTEXT.get();
        if (context == null) {
            context = new RequestContext();
            CONTEXT.set(context);
        }
        return context;
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T) attributes.get(name);
    }

    public Long getUserId() {
        return getAttribute("userId");
    }

    public String getUserRole() {
        return getAttribute("userRole");
    }
}
