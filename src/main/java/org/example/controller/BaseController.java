package org.example.controller;

import com.google.gson.Gson;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.core.RequestContext;

public abstract class BaseController implements Controller {
    protected final Gson gson;

    protected BaseController(Gson gson) {
        this.gson = gson;
    }

    protected Long getCurrentUserId() {
        return RequestContext.current().getUserId();
    }

    protected String getCurrentUserRole() {
        return RequestContext.current().getUserRole();
    }

    protected void sendJson(HttpResponse response, Object data) {
        response.contentType("application/json")
                .body(gson.toJson(data));
    }

    protected void sendError(HttpResponse response, int status, String message) {
        response.status(status)
                .contentType("application/json")
                .body(gson.toJson(java.util.Map.of("error", message)));
    }
}