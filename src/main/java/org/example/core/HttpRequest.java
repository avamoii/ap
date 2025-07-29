// 2. HTTP Request/Response Wrappers
package org.example.core;

import com.sun.net.httpserver.HttpExchange;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private final HttpExchange exchange;
    private final Map<String, String> pathParams = new HashMap<>();
    private final Map<String, String> queryParams = new HashMap<>();
    private String body;

    public HttpRequest(HttpExchange exchange) {
        this.exchange = exchange;
        parseQueryParams();
    }

    public String getMethod() {
        return exchange.getRequestMethod();
    }

    public URI getURI() {
        return exchange.getRequestURI();
    }

    public String getPath() {
        return exchange.getRequestURI().getPath();
    }

    public String getHeader(String name) {
        return exchange.getRequestHeaders().getFirst(name);
    }

    public String getBody() throws IOException {
        if (body == null) {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            body = sb.toString();
        }
        return body;
    }

    public void setPathParam(String name, String value) {
        pathParams.put(name, value);
    }

    public String getPathParam(String name) {
        return pathParams.get(name);
    }

    public String getQueryParam(String name) {
        return queryParams.get(name);
    }

    public Map<String, String> getQueryParams() {
        return new HashMap<>(queryParams);
    }

    private void parseQueryParams() {
        String query = exchange.getRequestURI().getQuery();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    queryParams.put(keyValue[0], keyValue[1]);
                }
            }
        }
    }
}