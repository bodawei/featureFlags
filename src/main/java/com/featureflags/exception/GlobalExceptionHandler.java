package com.featureflags.exception;

import com.featureflags.store.OptimisticConcurrencyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({FlagNotFoundException.class, EnvironmentNotFoundException.class, VariantNotFoundException.class})
    public ResponseEntity<ProblemDetail> handleNotFound(FeatureFlagDomainException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(FlagDeletedException.class)
    public ResponseEntity<ProblemDetail> handleGone(FlagDeletedException ex) {
        return ResponseEntity.status(HttpStatus.GONE)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.GONE, ex.getMessage()));
    }

    @ExceptionHandler({EnvironmentAlreadyExistsException.class, OptimisticConcurrencyException.class})
    public ResponseEntity<ProblemDetail> handleConflict(FeatureFlagDomainException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + " " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail));
    }
}
