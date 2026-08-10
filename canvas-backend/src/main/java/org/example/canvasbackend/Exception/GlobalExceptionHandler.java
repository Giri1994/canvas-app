package org.example.canvasbackend.Exception;

import org.example.canvasbackend.dto.CanvasResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CanvasResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return badRequest("Validation failed: " + message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CanvasResponse> handleMalformedJson(HttpMessageNotReadableException ex) {
        return badRequest("Malformed or missing request body");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<CanvasResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return badRequest("Invalid value for parameter '" + ex.getName() + "'");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CanvasResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return badRequest(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CanvasResponse> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new CanvasResponse("", "An unexpected error occurred: " + ex.getMessage(), false));
    }

    private ResponseEntity<CanvasResponse> badRequest(String message) {
        return ResponseEntity.badRequest().body(new CanvasResponse("", "Error: " + message, false));
    }
}
