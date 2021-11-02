package com.builder.exceptions;

/**

 */
public class RequestFieldNotPresent extends RuntimeException{
    public RequestFieldNotPresent(String message) {
        super(message);
    }

    public RequestFieldNotPresent(String message, Throwable cause) {
        super(message, cause);
    }
}
