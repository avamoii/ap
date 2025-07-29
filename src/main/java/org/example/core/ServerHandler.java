// 8. Main HTTP Server Handler
package org.example.core;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.exception.HttpException;
import org.example.middleware.Middleware;
import java.io.IOException;
import java.util.Map;

public class ServerHandler implements HttpHandler {
    private final Router router;
    private final Gson gson;

    public ServerHandler(Router router, Gson gson) {
        this.router = router;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpRequest request = new HttpRequest(exchange);
        HttpResponse response = new HttpResponse(exchange);

        try {
            // Clear any existing context
            RequestContext.clear();

            // Execute middlewares
            for (Middleware middleware : router.getMiddlewares()) {
                if (!middleware.handle(request, response)) {
                    break; // Middleware stopped the chain
                }
            }

            // Find and execute route
            Router.Route route = router.findRoute(request.getMethod(), request.getPath());
            if (route == null) {
                response.status(404)
                        .body(gson.toJson(Map.of("error", "Route not found")));
            } else {
                // Extract path parameters
                Map<String, String> pathParams = route.extractParams(request.getPath());
                pathParams.forEach(request::setPathParam);

                // Execute controller
                route.controller.handle(request, response);
            }

        } catch (HttpException e) {
            response.status(e.getStatusCode())
                    .body(gson.toJson(Map.of("error", e.getMessage())));
        } catch (Exception e) {
            e.printStackTrace();
            response.status(500)
                    .body(gson.toJson(Map.of("error", "An unexpected internal server error occurred.")));
        } finally {
            // Send response and cleanup
            response.send();
            RequestContext.clear();
        }
    }
}