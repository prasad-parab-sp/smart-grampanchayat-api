package com.asset.smartgrampanchayatapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.asset.smartgrampanchayatapi.exception.DistrictShardUnavailableException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(DistrictShardUnavailableException.class)
    public ResponseEntity<Void> handleDistrictShardUnavailable(DistrictShardUnavailableException ex) {
        log.warn("{}: {}", ex.getMessage(), ex.getCause() != null ? ex.getCause().getMessage() : "");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}
