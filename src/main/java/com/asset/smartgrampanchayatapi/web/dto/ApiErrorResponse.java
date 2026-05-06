package com.asset.smartgrampanchayatapi.web.dto;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Uniform JSON body for API errors (controllers, filters, and {@code @RestControllerAdvice}).
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorResponse(
        String timestamp,
        String path,
        int status,
        String error,
        String code,
        String message,
        List<FieldViolation> violations
) {

    public record FieldViolation(String field, String message) {
    }

    public static ApiErrorResponse of(HttpServletRequest request, HttpStatus httpStatus, String code, String message) {
        return new ApiErrorResponse(
                Instant.now().toString(),
                path(request),
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                code,
                message,
                List.of()
        );
    }

    public static ApiErrorResponse of(
            HttpServletRequest request,
            HttpStatus httpStatus,
            String code,
            String message,
            List<FieldViolation> violations
    ) {
        return new ApiErrorResponse(
                Instant.now().toString(),
                path(request),
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                code,
                message,
                violations == null ? List.of() : List.copyOf(violations)
        );
    }

    private static String path(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String uri = request.getRequestURI();
        String qs = request.getQueryString();
        return qs == null || qs.isBlank() ? uri : uri + "?" + qs;
    }
}
