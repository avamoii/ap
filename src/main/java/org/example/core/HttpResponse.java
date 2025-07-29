package org.example.core;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HttpResponse {
    private final HttpExchange exchange;
    private int statusCode = 200;
    private String contentType = "application/json";
    private String body = "";

    public HttpResponse(HttpExchange exchange) {
        this.exchange = exchange;
    }

    public HttpResponse status(int code) {
        this.statusCode = code;
        return this;
    }

    public HttpResponse contentType(String type) {
        this.contentType = type;
        return this;
    }

    public HttpResponse body(String body) {
        this.body = body;
        return this;
    }

    public void send() throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        byte[] responseBytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
