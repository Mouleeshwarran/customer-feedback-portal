
package com.feedback.portal.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, String>> error(
            HttpStatus status, String message) {

        String correlationId = UUID.randomUUID().toString();
        Map<String, String> body = new LinkedHashMap<>();
        body.put("status", String.valueOf(status.value()));
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("correlationId", correlationId);

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleStatus(
            ResponseStatusException ex) {

        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return error(status, ex.getReason() != null
            ? ex.getReason()
            : status.getReasonPhrase());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(field -> field.getField() + ": " + field.getDefaultMessage())
            .findFirst()
            .orElse("Invalid request data");

        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadable(
            HttpMessageNotReadableException ex) {
        return error(HttpStatus.BAD_REQUEST,
            "Request body is missing or invalid");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraint(
            ConstraintViolationException ex) {
        return error(HttpStatus.BAD_REQUEST,
            "Request validation failed");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDatabase(
            DataIntegrityViolationException ex) {
        return error(HttpStatus.CONFLICT,
            "The request conflicts with existing data");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxUpload(
            MaxUploadSizeExceededException ex) {
        return error(HttpStatus.BAD_REQUEST,
            "Uploaded file exceeds the maximum allowed size of 10 MB");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(
            IllegalArgumentException ex) {
        return error(HttpStatus.BAD_REQUEST,
            ex.getMessage() != null ? ex.getMessage() : "Invalid request");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(
            Exception ex) {
        // Log internally but return generic message to client
        return error(HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected server error occurred");
    }
}
