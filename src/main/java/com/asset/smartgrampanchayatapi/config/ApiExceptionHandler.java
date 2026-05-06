package com.asset.smartgrampanchayatapi.config;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.asset.smartgrampanchayatapi.exception.DistrictShardUnavailableException;
import com.asset.smartgrampanchayatapi.web.dto.ApiErrorResponse;
import com.asset.smartgrampanchayatapi.web.dto.ApiErrorResponse.FieldViolation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String message = ex.getReason();
        if (message == null || message.isBlank()) {
            message = status.getReasonPhrase();
        }
        String code = status.name();
        ApiErrorResponse body = ApiErrorResponse.of(request, status, code, message);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(DistrictShardUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleDistrictShardUnavailable(
            DistrictShardUnavailableException ex,
            HttpServletRequest request
    ) {
        log.warn("{}: {}", ex.getMessage(), ex.getCause() != null ? ex.getCause().getMessage() : "");
        String detail = ex.getCause() != null && ex.getCause().getMessage() != null
                ? ex.getCause().getMessage()
                : "";
        String message = detail.isBlank() ? ex.getMessage() : ex.getMessage() + ": " + detail;
        ApiErrorResponse body = ApiErrorResponse.of(
                request,
                HttpStatus.SERVICE_UNAVAILABLE,
                "DISTRICT_DATABASE_UNAVAILABLE",
                message
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.debug("Unreadable HTTP message: {}", ex.getMessage());
        String hint = ex.getMostSpecificCause().getMessage();
        if (hint == null || hint.isBlank()) {
            hint = "Request body could not be read";
        }
        ApiErrorResponse body = ApiErrorResponse.of(request, HttpStatus.BAD_REQUEST, "BAD_REQUEST_BODY", hint);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new FieldViolation(
                        fe.getField(),
                        fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid"
                ))
                .collect(Collectors.toList());
        String summary = violations.stream()
                .map(v -> v.field() + ": " + v.message())
                .collect(Collectors.joining("; "));
        if (summary.isBlank()) {
            summary = "Validation failed";
        }
        ApiErrorResponse body = ApiErrorResponse.of(
                request,
                HttpStatus.BAD_REQUEST,
                "VALIDATION_FAILED",
                summary,
                violations
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        List<FieldViolation> violations = ex.getConstraintViolations().stream()
                .map(this::toFieldViolation)
                .collect(Collectors.toList());
        String summary = violations.stream()
                .map(v -> v.field() + ": " + v.message())
                .collect(Collectors.joining("; "));
        ApiErrorResponse body = ApiErrorResponse.of(
                request,
                HttpStatus.BAD_REQUEST,
                "VALIDATION_FAILED",
                summary.isBlank() ? "Constraint validation failed" : summary,
                violations
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private FieldViolation toFieldViolation(ConstraintViolation<?> cv) {
        String path = cv.getPropertyPath() != null ? cv.getPropertyPath().toString() : "parameter";
        return new FieldViolation(path, cv.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {
        String message = "Required query parameter '" + ex.getParameterName() + "' is missing";
        ApiErrorResponse body = ApiErrorResponse.of(request, HttpStatus.BAD_REQUEST, "MISSING_PARAMETER", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String name = ex.getName();
        Object val = ex.getValue();
        String message = "Invalid value for '" + name + "'"
                + (val != null ? ": '" + val + "'" : "")
                + (ex.getRequiredType() != null ? " (expected " + ex.getRequiredType().getSimpleName() + ")" : "");
        ApiErrorResponse body = ApiErrorResponse.of(request, HttpStatus.BAD_REQUEST, "TYPE_MISMATCH", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request
    ) {
        String supported = ex.getSupportedHttpMethods() != null
                ? ex.getSupportedHttpMethods().stream().map(HttpMethod::name).collect(Collectors.joining(", "))
                : "";
        String message = "Method " + ex.getMethod() + " is not supported for this path"
                + (supported.isBlank() ? "" : ". Supported: " + supported);
        ApiErrorResponse body = ApiErrorResponse.of(request, HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", message);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResource(NoResourceFoundException ex, HttpServletRequest request) {
        ApiErrorResponse body = ApiErrorResponse.of(
                request,
                HttpStatus.NOT_FOUND,
                "NOT_FOUND",
                "No static resource for " + ex.getResourcePath()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * JDBC / shard errors that bubble outside {@link DistrictShardUnavailableException}.
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleDataAccess(DataAccessException ex, HttpServletRequest request) {
        log.warn("Data access error: {}", ex.getMessage());
        ApiErrorResponse body = ApiErrorResponse.of(
                request,
                HttpStatus.SERVICE_UNAVAILABLE,
                "DATA_ACCESS_ERROR",
                "A database error occurred. Try again later."
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnhandled(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception", ex);
        ApiErrorResponse body = ApiErrorResponse.of(
                request,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "An unexpected error occurred."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
