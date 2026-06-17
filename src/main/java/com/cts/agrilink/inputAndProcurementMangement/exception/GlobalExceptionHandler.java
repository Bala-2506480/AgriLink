package com.cts.agrilink.inputAndProcurementMangement.exception;

import com.cts.agrilink.inputAndProcurementMangement.dto.MessageResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

// Scoped to the input/procurement controllers so it does not clash with the
// IAM module's com.cts.agrilink.exception.GlobalExceptionHandler.
@RestControllerAdvice(basePackages = "com.cts.agrilink.inputAndProcurementMangement")
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<MessageResponseDTO> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MessageResponseDTO> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponseDTO(errors));
    }

    @ExceptionHandler(StateConflictException.class)
    public ResponseEntity<MessageResponseDTO> handleConflict(StateConflictException ex) {
        log.warn("State conflict: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new MessageResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MessageResponseDTO> handleBadRequest(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponseDTO(ex.getMessage()));
    }

    // Safety net: anything not handled above is a real server-side fault.
    // Log at ERROR with the stack trace so it is traceable in the logs.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponseDTO> handleUnexpected(Exception ex) {
        log.error("Unexpected error processing request", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponseDTO("An unexpected error occurred"));
    }
}