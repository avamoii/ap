package org.example;

import static spark.Spark.*;

public class Main {
    public static void main(String[] args) {
        port(8080); 

        get("/hello", (req, res) -> "helloooooooo");

        post("/echo", (req, res) -> {
    String body = req.body();
    if (body == null || body.isEmpty()) {
        return "no input received";
    }
    return "you said " + body;
});
  
        get("/", (req, res) -> {
            
            return "{\"message\":\"سرور بالا است\"}";
        });
    }
}
