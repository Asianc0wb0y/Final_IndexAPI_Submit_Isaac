package de.solactive.challenge.indexapi.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
public class ValidationErrorHandler {

    /**
     * Handles validation errors from @Valid annotations.
     *
     * @param ex The exception thrown when validation fails.
     * @return 400 Bad Request with an empty body.
     */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Void> handleValidationErrors(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().build();  // 400 Bad Request with empty body
    }
}
