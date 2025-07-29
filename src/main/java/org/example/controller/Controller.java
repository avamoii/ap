// 5. Controller Interface and Base Controller
package org.example.controller;

import org.example.core.HttpRequest;
import org.example.core.HttpResponse;

@FunctionalInterface
public interface Controller {
    void handle(HttpRequest request, HttpResponse response) throws Exception;
}