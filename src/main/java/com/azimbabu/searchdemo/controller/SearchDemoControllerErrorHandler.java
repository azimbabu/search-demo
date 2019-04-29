package com.azimbabu.searchdemo.controller;

import com.azimbabu.searchdemo.exception.SearchDemoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.constraints.NotNull;

@ControllerAdvice
public class SearchDemoControllerErrorHandler {

    @ExceptionHandler(SearchDemoException.class)
    public <T> ResponseEntity<T> handleException(@NotNull SearchDemoException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public <T> ResponseEntity<T> handleException(@NotNull IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}
