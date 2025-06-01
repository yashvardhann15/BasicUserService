package com.project.userservicejwt.Advices;

import com.project.userservicejwt.Exceptions.InvalidOrExpiredOTPException;
import com.project.userservicejwt.Exceptions.UserAlreadyExistsException;
import com.project.userservicejwt.Exceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

@org.springframework.web.bind.annotation.ControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler()
    public ResponseEntity<String> userAlreadyExists(UserAlreadyExistsException ex) {
        return new ResponseEntity<>("User already exists", HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    public ResponseEntity<String> userNotFound(UserNotFoundException ex) {
        return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<String> invalidOrExpiredOTP(InvalidOrExpiredOTPException ex) {
        return new ResponseEntity<>("OTP is either invalid or Expired, Retry", HttpStatus.FORBIDDEN);
    }
}