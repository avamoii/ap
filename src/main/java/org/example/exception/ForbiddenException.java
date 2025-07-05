// File: src/main/java/org/example/exception/ForbiddenException.java
package org.example.exception;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
