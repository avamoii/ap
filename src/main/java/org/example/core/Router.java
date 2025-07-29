// 7. Router - Handles route registration and matching
package org.example.core;

import org.example.controller.Controller;
import org.example.middleware.Middleware;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Router {
    private final List<Route> routes = new ArrayList<>();
    private final List<Middleware> middlewares = new ArrayList<>();

    public static class Route {
        final String method;
        final RoutePattern pattern;
        final Controller controller;

        public Route(String method, String path, Controller controller) {
            this.method = method;
            this.pattern = new RoutePattern(path);
            this.controller = controller;
        }

        public boolean matches(String method, String path) {
            return this.method.equals(method) && pattern.matches(path);
        }

        public Map<String, String> extractParams(String path) {
            return pattern.extractParams(path);
        }
    }

    public Router use(Middleware middleware) {
        middlewares.add(middleware);
        return this;
    }

    public Router get(String path, Controller controller) {
        routes.add(new Route("GET", path, controller));
        return this;
    }

    public Router post(String path, Controller controller) {
        routes.add(new Route("POST", path, controller));
        return this;
    }

    public Router put(String path, Controller controller) {
        routes.add(new Route("PUT", path, controller));
        return this;
    }

    public Router patch(String path, Controller controller) {
        routes.add(new Route("PATCH", path, controller));
        return this;
    }

    public Router delete(String path, Controller controller) {
        routes.add(new Route("DELETE", path, controller));
        return this;
    }

    public Route findRoute(String method, String path) {
        return routes.stream()
                .filter(route -> route.matches(method, path))
                .findFirst()
                .orElse(null);
    }

    public List<Middleware> getMiddlewares() {
        return middlewares;
    }
}