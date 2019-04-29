package com.azimbabu.searchdemo.exception;

public class SearchDemoException extends RuntimeException {

    public SearchDemoException(String message) {
        super(message);
    }

    public SearchDemoException(String message, Throwable cause) {
        super(message, cause);
    }
}
