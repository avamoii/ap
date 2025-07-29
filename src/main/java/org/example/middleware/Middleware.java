// 6. Middleware Interface and Implementations
package org.example.middleware;

import org.example.core.HttpRequest;
import org.example.core.HttpResponse;

@FunctionalInterface
public interface Middleware {
    boolean handle(HttpRequest request, HttpResponse response) throws Exception;
}