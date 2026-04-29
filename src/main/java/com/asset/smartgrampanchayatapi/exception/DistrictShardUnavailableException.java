package com.asset.smartgrampanchayatapi.exception;

/**
 * Thrown when the API cannot reach or query a district database (network, auth, SQL failure on shard).
 */
public class DistrictShardUnavailableException extends RuntimeException {

    public DistrictShardUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
