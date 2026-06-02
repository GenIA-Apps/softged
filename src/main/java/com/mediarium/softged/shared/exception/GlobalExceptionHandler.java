package com.mediarium.softged.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ProblemDetail> handleBadRequest(
            BadRequestException exception,
            HttpServletRequest request
    ) {
        return buildProblem(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildProblem(
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(ForbiddenResourceException.class)
    public ResponseEntity<ProblemDetail> handleForbiddenResource(
            ForbiddenResourceException exception,
            HttpServletRequest request
    ) {
        return buildProblem(
                HttpStatus.FORBIDDEN,
                "FORBIDDEN_RESOURCE",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        return buildProblem(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "Access denied",
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(
                        error.getField(),
                        error.getDefaultMessage()
                ));

        ProblemDetail problem = createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Request validation failed",
                request
        );

        problem.setProperty("errors", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getConstraintViolations()
                .forEach(violation -> errors.put(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                ));

        ProblemDetail problem = createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "CONSTRAINT_VIOLATION",
                "Request constraint validation failed",
                request
        );

        problem.setProperty("errors", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(problem);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ProblemDetail> handleHandlerMethodValidation(
            HandlerMethodValidationException exception,
            HttpServletRequest request
    ) {
        return buildProblem(
                HttpStatus.BAD_REQUEST,
                "METHOD_VALIDATION_ERROR",
                "Request validation failed",
                request
        );
    }

    @ExceptionHandler(TechnicalException.class)
    public ResponseEntity<ProblemDetail> handleTechnicalException(
            TechnicalException exception,
            HttpServletRequest request
    ) {
        return buildProblem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "TECHNICAL_ERROR",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(
            Exception exception,
            HttpServletRequest request
    ) {
        return buildProblem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                request
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadableException(
            Exception exception,
            HttpServletRequest request
    ) {
        return buildProblem(
                HttpStatus.BAD_REQUEST,
                "REQUEST_INVALID",
                exception.getMessage(),
                request
        );
    }

    private ResponseEntity<ProblemDetail> buildProblem(
            HttpStatus status,
            String code,
            String detail,
            HttpServletRequest request
    ) {
        ProblemDetail problem = createProblemDetail(
                status,
                code,
                detail,
                request
        );

        return ResponseEntity
                .status(status)
                .body(problem);
    }

    private ProblemDetail createProblemDetail(
            HttpStatus status,
            String code,
            String detail,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);

        problem.setTitle(status.getReasonPhrase());
        problem.setType(URI.create("https://softged.mediarium.local/problems/" + code.toLowerCase()));
        problem.setInstance(URI.create(request.getRequestURI()));

        problem.setProperty("code", code);
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("path", request.getRequestURI());

        return problem;
    }
}