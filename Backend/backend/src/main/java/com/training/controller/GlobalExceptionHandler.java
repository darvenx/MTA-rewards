package com.training.controller;

import com.training.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Object> createErrorResponse(String message, HttpStatus status) {
        return new ResponseEntity<>(Collections.singletonMap("message", message), status);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException() {
        return createErrorResponse("User not found", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Object> handleUserAlreadyExistsException() {
        return createErrorResponse("User Already Exists", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<Object> handleSignatureException() {
        return createErrorResponse("Invalid JWT signature", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<Object> handleMalformedJwtException() {
        return createErrorResponse("Invalid JWT token", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Object> handleExpiredJwtException() {
        return createErrorResponse("JWT token has expired", HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Object> handleInvalidCredentialsException() {
        return createErrorResponse("Wrong credentials", HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(DuplicateTransferException.class)
    public ResponseEntity<Object> handleDuplicateTransferException() {
        return createErrorResponse("Duplicate Transaction", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<Map<String, String>> fieldErrors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("field", fieldName);
            errorMap.put("message", errorMessage);
            fieldErrors.add(errorMap);
        });
        return new ResponseEntity<>(Collections.singletonMap("fieldErrors", fieldErrors), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SelfTransferException.class)
    public ResponseEntity<Object> handleSelfTransferException(){
        return new ResponseEntity<>("Cant Transfer to self",HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IncorrectPinException.class)
    public ResponseEntity<Object> handleIncorrectPinException(){
        return new ResponseEntity<>("Invalid Pin",HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Object> handleInsufficientBalance(){
        return new ResponseEntity<>("Not enough balance",HttpStatus.BAD_REQUEST);
    }
}