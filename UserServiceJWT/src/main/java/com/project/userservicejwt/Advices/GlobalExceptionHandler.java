package com.project.userservicejwt.Advices;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoHandlerFoundException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", "Not Found");
        body.put("message", "The requested URL was not found on this server.");
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        return new ResponseEntity<>(ex.getMessage() , HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
